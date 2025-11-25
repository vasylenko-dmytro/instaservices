package com.vasylenko.edu.ig.to.kafka.service.listener;

import com.vasylenko.edu.ig.to.kafka.service.model.IGPost;

public interface IGHashtagListener {
    void onNewPost(IGPost post);
}
