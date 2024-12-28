package ru.lr.shop.alert;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AlertService implements IAlertService {
    private static final Logger log = LoggerFactory.getLogger(AlertService.class);
    private final List<Alert> alerts = new CopyOnWriteArrayList<>();

    @Override
    public void makeAlert(String msg) {
        log.warn("Got Alert. {}", msg);
        alerts.add(createAlert(msg));
    }

    private Alert createAlert(String msg) {
        return new Alert(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE), msg);
    }

    @Override
    public List<Alert> getAllAlerts() {
        return List.copyOf(alerts);
    }
}
