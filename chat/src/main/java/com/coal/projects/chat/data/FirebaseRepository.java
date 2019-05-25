package com.coal.projects.chat.data;


import android.util.Log;
import com.coal.projects.chat.data.pojo.Chat;
import com.coal.projects.chat.data.pojo.User;
import com.coal.projects.chat.domain.mappers.LoginsToDisplayNamesMapper;
import com.coal.projects.chat.firestore_constants.Chats;
import com.coal.projects.chat.firestore_constants.Users;
import com.coal.projects.chat.presentation.chats.CreatedChat;
import com.coal.projects.chat.presentation.contacts.SelectableUser;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;

import java.text.SimpleDateFormat;
import java.util.*;

/*
* Несколько слов об этой сущности
*
* Так как Firestore - нереляционная база данных, скорость получения резулятата обратно
* пропорциональна сложности выборки. Иначе говоря, чем сложнее выборка, тем уродливей будет выглядеть запрос.
* Далее, возле особо уродливых запросов будут оставлены пояснения
* По хорошему, стоит выделять саму логику в одельные классы, но это был прототип и никому это не надо*/

public class FirebaseRepository {

    private static final String MESSAGE_NULL_USER = "ChatUser must be init first !";

    private FirebaseFirestore database;
    private String deviceToken;
    private ChatUser chatUser;

    /*В данном случает, Реплэи выбраны из-за специфики получения данных.
    * Сначала возвращаем источник, потом в него эммитим, потом подписываемся на источник
    * Реплай отправит все данные в потоке, даже если сетевой запрос отработает быстрее чем
    * return для источника*/

    private ReplaySubject<List<Map<String, Object>>> userChatsObservable = ReplaySubject.create();
    private ReplaySubject<CreatedChat> createNewChatObservable = ReplaySubject.create();
    private ReplaySubject<Boolean> connectToFirestoreObservable = ReplaySubject.create();
    private ReplaySubject<List<SelectableUser>> contactsObservable = ReplaySubject.create();
    private ReplaySubject<Boolean> addContactObservable = ReplaySubject.create();
    private ReplaySubject<Boolean> findUserByEmailObservable = ReplaySubject.create();

    private PublishSubject<ArrayList<HashMap<String, String>>> chatMessagesObservable = PublishSubject.create();

    public FirebaseRepository(FirebaseFirestore database) {
        this.database = database;
    }

    public void initChatUser(ChatUser chatUser) {
        this.chatUser = chatUser;
    }

    private DocumentReference getCurrentUserReference() {
        if (chatUser == null)
            throw new NullPointerException(MESSAGE_NULL_USER);
        return database.collection(Users.COLLECTION_PATH)
                .document(chatUser.getLogin());
    }

    private DocumentReference getuserByLogin(String login) {
        return database.collection(Users.COLLECTION_PATH)
                .document(login);
    }

    public Observable<List<Map<String, Object>>> observeUserChats() {

        PublishSubject<Map<String, Object>> snapshotPublishSubject = PublishSubject.create();

        database.collection(Chats.COLLECTION_PATH)
                .whereArrayContains(Chats.FIELD_MEMBERS, chatUser.getLogin())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.getDocuments().isEmpty()) {

                        //List, содержащий все снапшоты чатов, в которых состоит юзер
                        List<DocumentSnapshot> listSnapshots = querySnapshot.getDocuments();
                        CompositeDisposable disposable = new CompositeDisposable();

                        for (DocumentSnapshot item : listSnapshots) {

                            //Вешаем на каждую ссылку чата наблюдателя
                            database.collection(Chats.COLLECTION_PATH)
                                    .document(item.getId())
                                    .addSnapshotListener((snapshot1, e) -> {
                                        if (e != null || snapshot1 == null)
                                            snapshotPublishSubject.onError(e);

                                        HashMap<String, Object> map = new HashMap<>();

                                        //Маппер вернет Observable<List<String>> - отображаемые в чате имена
                                        disposable.add(new LoginsToDisplayNamesMapper(database)
                                                .map((List<String>) snapshot1.get(Chats.FIELD_MEMBERS), chatUser.getDisplayName())
                                                .subscribe(names -> {

                                                    //Создаем модельку для элемента списка чатов
                                                    map.put(Chats.FIELD_DISPLAY_NAMES, names);
                                                    map.put(Chats.FIELD_MEMBERS, snapshot1.get(Chats.FIELD_MEMBERS));
                                                    map.put(Chats.FIELD_MESSAGES, snapshot1.get(Chats.FIELD_MESSAGES));
                                                    map.put(Chats.FIELD_CHAT_ID, snapshot1.get(Chats.FIELD_CHAT_ID));
                                                    snapshotPublishSubject.onNext(map);
                                                }, excp -> {
                                                    excp.printStackTrace();
                                                    snapshotPublishSubject.onNext(new HashMap<>());
                                                }));
                                    });
                        }

                        //Конвертим данные в List<HashMap<String, Object>> и отменяем подписки на маппер имен
                        snapshotPublishSubject
                                .buffer(listSnapshots.size())
                                .map(list -> {
                                    disposable.dispose();
                                    return list;
                                })
                                .subscribe(userChatsObservable::onNext, contactsObservable::onError);
                    } else
                        userChatsObservable.onNext(new ArrayList<>());
                })
                .addOnFailureListener(e -> userChatsObservable.onError(e));
        return userChatsObservable;
    }

    public Observable<String> getProfileUrl(String login) {

        BehaviorSubject<String> userImage = BehaviorSubject.create();
        database.collection(Users.COLLECTION_PATH)
                .document(login)
                .get()
                .addOnSuccessListener(snapshot -> userImage.onNext((String) snapshot.get(Users.FIELD_PHOTO_URL)))
                .addOnFailureListener(userImage::onError);

        return userImage;
    }

    public Observable<CreatedChat> createNewChat(List<String> chatMembers) {
        chatMembers.add(chatUser.getLogin());
        DocumentReference chatDocument = database.collection(Chats.COLLECTION_PATH).document();
        String chatId = chatDocument.getId();
        chatDocument
                .set(new Chat(chatMembers, new ArrayList<>(), chatId))
                .addOnCompleteListener(task -> getuserByLogin(chatMembers.get(0)).get().addOnSuccessListener(snapshot -> {
                    createNewChatObservable.onNext(new CreatedChat((String) snapshot.get(Users.FIELD_DISPLAY_NAME), chatId));
                }))
                .addOnFailureListener(e -> createNewChatObservable.onError(e));
        return createNewChatObservable;
    }

    public Observable<Boolean> connectToFirestore() {
        FirebaseInstanceId
                .getInstance()
                .getInstanceId()
                .addOnSuccessListener(instanceIdResult -> {
                    deviceToken = instanceIdResult.getToken();
                    getCurrentUserReference()
                            .addSnapshotListener(this::updateDeviceTokenOrCreateNewUser);
                })
                .addOnFailureListener(e -> connectToFirestoreObservable.onError(e));
        return connectToFirestoreObservable;
    }

    private void updateDeviceTokenOrCreateNewUser(DocumentSnapshot snapshot, Throwable e) {
        if (chatUser == null) {
            throw new NullPointerException(MESSAGE_NULL_USER);
        }
        if (e != null) {
            connectToFirestoreObservable.onError(e);
        } else {
            if (snapshot != null && snapshot.get(Users.FIELD_DEVICE_TOKEN) != null) {
                HashMap<String, Object> updateTokenMap = new HashMap<>();
                updateTokenMap.put(Users.FIELD_DEVICE_TOKEN, deviceToken);
                getCurrentUserReference()
                        .update(updateTokenMap)
                        .addOnSuccessListener(aVoid -> connectToFirestoreObservable.onNext(true))
                        .addOnFailureListener(e1 -> connectToFirestoreObservable.onError(e1));

            } else {
                User user = new User(
                        chatUser.getDisplayName(),
                        deviceToken,
                        chatUser.getLogin(),
                        chatUser.getPhotoUrl());
                getCurrentUserReference()
                        .set(user)
                        .addOnSuccessListener(aVoid -> connectToFirestoreObservable.onNext(true))
                        .addOnFailureListener(e2 -> connectToFirestoreObservable.onError(e2));
            }
        }
    }

    public Observable<List<SelectableUser>> observeUserContacts() {
        if (chatUser == null)
            throw new NullPointerException(MESSAGE_NULL_USER);
        getCurrentUserReference().addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                contactsObservable.onError(e);
            } else {
                PublishSubject<DocumentSnapshot> snapshotPublishSubject = PublishSubject.create();
                List<String> contacts = (List<String>) documentSnapshot.get(Users.FIELD_CONTACTS);

                if (contacts != null && !contacts.isEmpty()) {
                    for (String login : contacts) {
                        Log.e("login", login);
                        database.collection(Users.COLLECTION_PATH)
                                .document(login)
                                .get()
                                .addOnSuccessListener(snapshotPublishSubject::onNext)
                                .addOnFailureListener(snapshotPublishSubject::onError);
                    }
                    snapshotPublishSubject
                            .buffer(contacts.size())
                            .map(list -> {
                                Log.e("gets list", list.toString());
                                List<SelectableUser> selectableUsers = new ArrayList<>();
                                for (DocumentSnapshot snap : list) {
                                    SelectableUser user = new SelectableUser(
                                            (String) snap.get(Users.FIELD_DISPLAY_NAME),
                                            (String) snap.get(Users.FIELD_DEVICE_TOKEN),
                                            (String) snap.get(Users.FIELD_LOGIN),
                                            (String) snap.get(Users.FIELD_PHOTO_URL));
                                    selectableUsers.add(user);
                                }
                                return selectableUsers;
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io())
                            .subscribe(contactsObservable::onNext, contactsObservable::onError);

                } else {
                    Log.e("list contacts", "NULL or EMPTY");
                    contactsObservable.onNext(new ArrayList<>());
                }
            }
        });
        return contactsObservable;
    }

    public Observable<Boolean> addContact(String contact) {
        if (chatUser == null)
            throw new NullPointerException(MESSAGE_NULL_USER);
        HashMap<String, Object> user = new HashMap<>();
        getCurrentUserReference()
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot != null) {
                        try {
                            ArrayList<String> contacts = ((ArrayList<String>) snapshot.get(Users.FIELD_CONTACTS));
                            if (contacts != null) {
                                contacts.add(contact);
                                user.put(Users.FIELD_CONTACTS, contacts);
                                getCurrentUserReference().update(user);
                            }
                        } catch (ClassCastException e) {
                            addContactObservable.onError(e);
                        }
                    }
                }).addOnFailureListener(e -> addContactObservable.onError(e));
        return addContactObservable;
    }

    public Observable<Boolean> findUserByEmail(String text) {
        if (text != null && !text.isEmpty())
            database.collection(Users.COLLECTION_PATH)
                    .whereEqualTo(Users.FIELD_LOGIN, text)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot != null && !snapshot.getDocuments().isEmpty()) {
                            for (DocumentSnapshot snap : snapshot.getDocuments()) {
                                if (snap.get(Users.FIELD_LOGIN) != null && snap.get(Users.FIELD_LOGIN).equals(text))
                                    findUserByEmailObservable.onNext(true);
                            }
                        } else {
                            findUserByEmailObservable.onNext(false);
                        }
                    })
                    .addOnFailureListener(e -> findUserByEmailObservable.onError(e));
        return findUserByEmailObservable;
    }

    public Observable<ArrayList<HashMap<String, String>>> observeChatMessages(String chatId) {
        PublishSubject<HashMap<String, String>> publishSubject = PublishSubject.create();
        database.collection(Chats.COLLECTION_PATH)
                .document(chatId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null)
                        chatMessagesObservable.onError(e);

                    List<HashMap<String, Object>> receivedMessages =
                            (List<HashMap<String, Object>>) documentSnapshot.get(Chats.FIELD_MESSAGES);

                    if (receivedMessages != null && !receivedMessages.isEmpty()) {

                        for (HashMap<String, Object> message : receivedMessages) {

                            getuserByLogin((String) message.get(Chats.FIELD_MESSAGE_OWNER))
                                    .addSnapshotListener((snapshot, exception) -> {
                                        if (exception != null)
                                            publishSubject.onError(exception);

                                        LinkedHashMap<String, String> newObject = new LinkedHashMap<>();
                                        newObject.put(Chats.FIELD_MESSAGE_TEXT, (String) message.get(Chats.FIELD_MESSAGE_TEXT));
                                        newObject.put(Chats.FIELD_MESSAGE_TIME, (String) message.get(Chats.FIELD_MESSAGE_TIME));
                                        newObject.put(Chats.FIELD_MESSAGE_OWNER, (String) message.get(Chats.FIELD_MESSAGE_OWNER));
                                        newObject.put(Users.FIELD_DISPLAY_NAME, (String) snapshot.get(Users.FIELD_DISPLAY_NAME));
                                        publishSubject.onNext(newObject);
                                    });
                        }
                        publishSubject
                                .buffer(receivedMessages.size())
                                .map(ArrayList::new)
                                .subscribe(
                                        list -> chatMessagesObservable.onNext(list),
                                        t -> chatMessagesObservable.onError(t));
                    }
                });
        return chatMessagesObservable;
    }

    public String getLogin() {
        return chatUser.getLogin();
    }

    public void sendMessage(String text, String chatId) {
        database.collection(Chats.COLLECTION_PATH)
                .document(chatId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    try {
                        ArrayList<HashMap<String, String>> messages =
                                (ArrayList<HashMap<String, String>>) documentSnapshot.get(Chats.FIELD_MESSAGES);
                        if (messages != null) {
                            HashMap<String, String> message = new HashMap<>();
                            message.put(Chats.FIELD_MESSAGE_READ, "false");
                            message.put(Chats.FIELD_MESSAGE_TEXT, text);
                            message.put(Chats.FIELD_MESSAGE_OWNER, chatUser.getLogin());
                            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.US);
                            Date date = new Date();
                            message.put(Chats.FIELD_MESSAGE_TIME, formatter.format(date));

                            messages.add(message);
                            database.collection(Chats.COLLECTION_PATH)
                                    .document(chatId)
                                    .update(Chats.FIELD_MESSAGES, messages);
                        }
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                    }
                });
    }

    //Удаляет из листа логин юзера. Нужен при получении мен в чатах
    public List<String> clear(List<String> temp) {
        ArrayList<String> logins = new ArrayList<>(temp);
        if (logins.contains(chatUser.getLogin())) {
            temp.remove(chatUser.getLogin());
        }
        return temp;
    }

    public Observable<List<String>> getLogins(String chatId) {
        PublishSubject<List<String>> chatLoginsObservable = PublishSubject.create();
        database
                .collection(Chats.COLLECTION_PATH)
                .document(chatId)
                .get()
                .onSuccessTask(snapshot -> {
                    chatLoginsObservable.onNext((List<String>) snapshot.get(Chats.FIELD_MEMBERS));
                    return null;
                });
        return chatLoginsObservable;
    }

    public Observable<String> getToken(String login) {
        PublishSubject<String> tokenObservable = PublishSubject.create();
        database
                .collection(Users.COLLECTION_PATH)
                .document(login)
                .get()
                .onSuccessTask(snapshot -> {
                    tokenObservable.onNext((String) snapshot.get(Users.FIELD_DEVICE_TOKEN));
                    return null;
                });
        return tokenObservable;
    }

    public ChatUser getChatUser() {
        return chatUser;
    }

    public Task<Void> removeChat(String s) {
        return database.collection(Chats.COLLECTION_PATH)
                .document(s)
                .delete();
    }
}
