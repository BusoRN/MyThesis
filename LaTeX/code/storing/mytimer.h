#ifndef MYTIMER_H
#define MYTIMER_H

#include <QObject>
#include <QtCore>
#include <downloader.h>

class MyTimer : public QObject
{
    Q_OBJECT
public:
    MyTimer();
    ~MyTimer();
    QTimer *timer;


public slots:
    void timerSlot();
private:
    Downloader d;
};

#endif // MYTIMER_H
