package ru.lr.shop.alert;

import java.util.List;

public interface IAlertService {
    void makeAlert(String msg);

    List<Alert> getAllAlerts();
}
