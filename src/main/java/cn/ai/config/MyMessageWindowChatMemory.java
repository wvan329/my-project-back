package cn.ai.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//自带的记忆会自动把数据库里之前的对话也删了，我觉得所有对话都可以保存下来，所以自己改造了

public final class MyMessageWindowChatMemory implements ChatMemory {

    private static final int DEFAULT_MAX_MESSAGES = 20;

    private final ChatMemoryRepository chatMemoryRepository;

    private final int maxMessages;

    private MyMessageWindowChatMemory(ChatMemoryRepository chatMemoryRepository, int maxMessages) {
        Assert.notNull(chatMemoryRepository, "chatMemoryRepository cannot be null");
        Assert.isTrue(maxMessages > 0, "maxMessages must be greater than 0");
        this.chatMemoryRepository = chatMemoryRepository;
        this.maxMessages = maxMessages;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void add(String conversationId, List<Message> newMessages) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Assert.notNull(newMessages, "messages cannot be null");
        Assert.noNullElements(newMessages, "messages cannot contain null elements");

        List<Message> memoryMessages = this.chatMemoryRepository.findByConversationId(conversationId);

        List<Message> processedMessages = new ArrayList<>();
        Set<Message> memoryMessagesSet = new HashSet<>(memoryMessages);
        boolean hasNewSystemMessage = newMessages.stream()
                .filter(SystemMessage.class::isInstance)
                .anyMatch(message -> !memoryMessagesSet.contains(message));

        memoryMessages.stream()
                .filter(message -> !(hasNewSystemMessage && message instanceof SystemMessage))
                .forEach(processedMessages::add);

        processedMessages.addAll(newMessages);

        this.chatMemoryRepository.saveAll(conversationId, processedMessages);
    }

    @Override
    public List<Message> get(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        List<Message> messages = this.chatMemoryRepository.findByConversationId(conversationId);
        int size = messages.size();
        int fromIndex = Math.max(size - maxMessages, 0);
        return messages.subList(fromIndex, size);
    }

    @Override
    public void clear(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        this.chatMemoryRepository.deleteByConversationId(conversationId);
    }

    private List<Message> process(List<Message> memoryMessages, List<Message> newMessages) {
        List<Message> processedMessages = new ArrayList<>();

        Set<Message> memoryMessagesSet = new HashSet<>(memoryMessages);
        boolean hasNewSystemMessage = newMessages.stream()
                .filter(SystemMessage.class::isInstance)
                .anyMatch(message -> !memoryMessagesSet.contains(message));

        memoryMessages.stream()
                .filter(message -> !(hasNewSystemMessage && message instanceof SystemMessage))
                .forEach(processedMessages::add);

        processedMessages.addAll(newMessages);

        if (processedMessages.size() <= this.maxMessages) {
            return processedMessages;
        }

        int messagesToRemove = processedMessages.size() - this.maxMessages;

        List<Message> trimmedMessages = new ArrayList<>();
        int removed = 0;
        for (Message message : processedMessages) {
            if (message instanceof SystemMessage || removed >= messagesToRemove) {
                trimmedMessages.add(message);
            } else {
                removed++;
            }
        }

        return trimmedMessages;
    }

    public static final class Builder {

        private ChatMemoryRepository chatMemoryRepository;

        private int maxMessages = DEFAULT_MAX_MESSAGES;

        private Builder() {
        }

        public Builder chatMemoryRepository(ChatMemoryRepository chatMemoryRepository) {
            this.chatMemoryRepository = chatMemoryRepository;
            return this;
        }

        public Builder maxMessages(int maxMessages) {
            this.maxMessages = maxMessages;
            return this;
        }

        public MyMessageWindowChatMemory build() {
            if (this.chatMemoryRepository == null) {
                this.chatMemoryRepository = new InMemoryChatMemoryRepository();
            }
            return new MyMessageWindowChatMemory(this.chatMemoryRepository, this.maxMessages);
        }

    }

}
