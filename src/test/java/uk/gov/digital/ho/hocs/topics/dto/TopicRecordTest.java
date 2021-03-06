package uk.gov.digital.ho.hocs.topics.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.topics.model.Topic;

import static org.assertj.core.api.Assertions.assertThat;

public class TopicRecordTest {

    @Test
    public void create() throws Exception {
        Topic topic = new Topic("TopicName", "OwningUnit","OwningTeam");

        TopicRecord topicRecord = TopicRecord.create(topic);

        assertThat(topicRecord.getTopicName()).isEqualTo("TopicName");
        assertThat(topicRecord.getTopicUnit()).isEqualTo("OwningUnit");
        assertThat(topicRecord.getTopicTeam()).isEqualTo("OwningTeam");
    }
}