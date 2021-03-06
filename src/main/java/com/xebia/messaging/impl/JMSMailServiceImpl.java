package com.xebia.messaging.impl;

import com.xebia.dao.AssetHistoryDAO;
import com.xebia.dao.AssignAssetMailDAO;
import com.xebia.entities.AssetHistory;
import com.xebia.entities.AssignAssetMail;
import com.xebia.entities.EventMail;
import com.xebia.enums.AssetStatus;
import com.xebia.enums.MailStatus;
import com.xebia.messaging.JMSMailService;
import com.xebia.services.IEventMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Pgupta on 02-08-2016.
 */
@Service
public class JMSMailServiceImpl implements JMSMailService {

    @Autowired
    JmsTemplate template;

    @Autowired
    AssignAssetMailDAO assignAssetMailDAO;

    @Autowired
    AssetHistoryDAO assetHistoryDAO;

    @Autowired
    IEventMailService eventMailService;


    @Scheduled(fixedRate = 20000)
    @Override
    public void processUnsentAssetMails() {
        List<AssignAssetMail> unsentMails = assignAssetMailDAO.getMailObjectsByStatus(MailStatus.NOT_SENT.getValue(), MailStatus.PENDING.getValue());
        List<AssignAssetMail> unsentAssignAssetMails = unsentMails
                .stream()
                .filter(e ->
                        e.getAssetStatus().equals(AssetStatus.ISSUED.getValue()))
                .collect(Collectors.toList());

        List<AssignAssetMail> unsentExpiringAssetMails = unsentMails
                .stream()
                .filter(e ->
                        e.getAssetStatus().equals(AssetStatus.EXPIRING.getValue()))
                .collect(Collectors.toList());

        List<AssignAssetMail> unsentExpiredAssetMails = unsentMails
                .stream()
                .filter(e ->
                        e.getAssetStatus().equals(AssetStatus.EXPIRED.getValue()))
                .collect(Collectors.toList());

        List<AssignAssetMail> unsentReturnedAssetMails = unsentMails
                .stream()
                .filter(e ->
                        e.getAssetStatus().equals(AssetStatus.RETURNED.getValue()))
                .collect(Collectors.toList());

        if (unsentAssignAssetMails.size() > 0)
            template.convertAndSend("assignAssetMailQueue", unsentAssignAssetMails);

        if (unsentExpiringAssetMails.size() > 0)
            template.convertAndSend("expiringAssetMailQueue", unsentExpiringAssetMails);

        if (unsentExpiredAssetMails.size() > 0)
            template.convertAndSend("expiredAssetMailQueue", unsentExpiredAssetMails);

        if (unsentReturnedAssetMails.size() > 0)
            template.convertAndSend("returnedAssetMailQueue", unsentReturnedAssetMails);
    }

    @Scheduled(fixedRate = 40000)
    @Override
    public void processUnsentEventMails() {
        List<EventMail> unsentEventMail = eventMailService.fetchMailObjectsNeedToSend();
        if (unsentEventMail.size() >= 1)
            template.convertAndSend("unsentEventMailQueue", unsentEventMail);
    }

    @PostConstruct
    @Override
    public void registerGettingExpiredAssetMail() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.afterPropertiesSet();
        scheduler.schedule(() -> {
            Date input = new Date();
            int daysToExpire = 2;
            List<AssignAssetMail> mailDtos = new ArrayList<>();
            int dayToExpire = daysToExpire;
            while (dayToExpire >= 0) {
                LocalDate todayDateTemp = input.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate expiryDateTemp = todayDateTemp.plus(dayToExpire, ChronoUnit.DAYS);
                Date expiryDate = Date.from(expiryDateTemp.atStartOfDay(ZoneId.systemDefault()).toInstant());
                List<AssetHistory> goingToExpireHistory = assetHistoryDAO.getGoingToExpireAssetHistory(expiryDate);
                for (AssetHistory history : goingToExpireHistory) {
                    AssignAssetMail mailDto = new AssignAssetMail();
                    mailDto.setAssetStatus(AssetStatus.EXPIRING.getValue());
                    mailDto.setAsset(history.getAsset());
                    mailDto.setEmployee(history.getEmployee2());
                    mailDto.setApprover(history.getEmployee1());
                    mailDto.setDateTillValid(history.getValidTill());
                    mailDto.setDateOfIssue(history.getIssueDate());
                    mailDto.setStatus(MailStatus.NOT_SENT.getValue());
                    mailDto.setUpdatedDate(new Date());
                    mailDtos.add(mailDto);
                }
                dayToExpire--;
            }
            template.convertAndSend("registerAssetExpiryMailQueue", mailDtos);


        }, new CronTrigger("00 30 09 * * *"));

    }

    @PostConstruct
    @Override
    public void registerExpiredAssetMail() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.afterPropertiesSet();
        scheduler.schedule(() -> {
            Date input = new Date();
            List<AssignAssetMail> mailDtos = new ArrayList<>();
            LocalDate todayDateTemp = input.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            Date expiryDate = Date.from(todayDateTemp.atStartOfDay(ZoneId.systemDefault()).toInstant());

            List<AssetHistory> expiredHistory = assetHistoryDAO.getExpiredAssetHistory(expiryDate);
            for (AssetHistory history : expiredHistory) {
                AssignAssetMail mailDto = new AssignAssetMail();
                mailDto.setAssetStatus(AssetStatus.EXPIRED.getValue());
                mailDto.setAsset(history.getAsset());
                mailDto.setEmployee(history.getEmployee2());
                mailDto.setApprover(history.getEmployee1());
                mailDto.setDateTillValid(history.getValidTill());
                mailDto.setDateOfIssue(history.getIssueDate());
                mailDto.setStatus(MailStatus.NOT_SENT.getValue());
                mailDtos.add(mailDto);
                history.setStatus(AssetStatus.EXPIRED.getValue());
                mailDto.setUpdatedDate(new Date());
                assetHistoryDAO.update(history);
            }
            template.convertAndSend("registerAssetExpiredMailQueue", mailDtos);


        }, new CronTrigger("00 30 09 * * *"));

    }

    @Override
    public void registerAssignAssetMail(AssignAssetMail assignAssetMail) {
        template.convertAndSend("registerAssignAssetMailQueue", assignAssetMail);
    }

    @Override
    public void registerReturnedAssetMail(AssignAssetMail assignAssetMail) {
        template.convertAndSend("registerReturnedAssetMailQueue", assignAssetMail);
    }

    @Override
    public void registerToMailQueue(EventMail eventMail) {
        template.convertAndSend("mailQueue", eventMail);
    }

}
