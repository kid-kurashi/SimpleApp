package com.coal.projects.chat.domain.mappers;

import com.coal.projects.chat.firestore_constants.Users;
import com.google.firebase.firestore.FirebaseFirestore;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.List;

public class LoginsToDisplayNamesMapper {
    private final FirebaseFirestore database;

    public LoginsToDisplayNamesMapper(FirebaseFirestore database) {
        this.database = database;

    }

    public Observable<List<String>> map(List<String> logins, String filter) {
        PublishSubject<String> namesStream = PublishSubject.create();
        if (logins != null && !logins.isEmpty()) {
            for (String login : logins) {
                database.collection(Users.COLLECTION_PATH)
                        .document(login)
                        .addSnapshotListener((snapshot, e) -> {
                            if (e != null)
                                namesStream.onError(e);
                            else {
                                if (((String) snapshot.get(Users.FIELD_DISPLAY_NAME)) != null)
                                    namesStream.onNext((String) snapshot.get(Users.FIELD_DISPLAY_NAME));
                                else
                                    namesStream.onError(new NullPointerException("Names stream has null"));
                            }
                        });
            }
            return namesStream
                    .map(s -> s.equals(filter) ? "" : s)
                    .buffer(logins.size());
        } else {
            return Observable.just(new ArrayList<>());
        }
    }
}
