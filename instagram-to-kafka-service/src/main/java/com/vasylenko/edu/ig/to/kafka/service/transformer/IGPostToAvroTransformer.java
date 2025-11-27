package com.vasylenko.edu.ig.to.kafka.service.transformer;

import com.vasylenko.edu.avro.model.IGAvroModel;
import com.vasylenko.edu.ig.to.kafka.service.model.IGPost;
import org.springframework.stereotype.Component;

@Component
public class IGPostToAvroTransformer {

    public IGAvroModel getAvroModelFromIGPost(IGPost igPost) {
        return IGAvroModel.newBuilder()
                .setId(igPost.getId())
                .setPermalink(igPost.getPermalink())
                .setCaption(igPost.getCaption())
                .setTimestamp(igPost.getTimestamp())
                .build();
    }
}
