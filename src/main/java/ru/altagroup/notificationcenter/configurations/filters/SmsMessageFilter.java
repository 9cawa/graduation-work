package ru.altagroup.notificationcenter.configurations.filters;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import ru.altagroup.notificationcenter.entities.Notice;
import ru.altagroup.notificationcenter.entities.NoticeSetting;
import ru.altagroup.notificationcenter.entities.Recipient;
import ru.altagroup.notificationcenter.repositories.NoticeSettingRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SmsMessageFilter implements ChainFilter {

    private ChainFilter chain;
    private final NoticeSettingRepository noticeSettingRepository;

    @Override
    public void setNextChain(ChainFilter nextChain) {
        this.chain = nextChain;
    }

    @Override
    public boolean filter(Message<?> message) {
        Recipient recipient = message.getHeaders().get("recipient", Recipient.class);
        Notice notice = message.getHeaders().get("notice", Notice.class);
        assert recipient != null;
        Optional<NoticeSetting> optionalNoticeSetting = noticeSettingRepository.findByRecipient_IdAndNotice(recipient.getId(), notice);
        if (chain == null || (optionalNoticeSetting.isPresent() && !optionalNoticeSetting.get().getBySms())) return false;
        return chain.filter(message);
    }
}
