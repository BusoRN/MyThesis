#ifndef MYTYMER_H
#define MYTYMER_H

#include <QtCore>


class MyTymer : public QObject
{
    Q_OBJECT
public:
    MyTymer();

    QTimer *timer;
    ~MyTymer();

public slots:
    void timerSlot();
};

#endif // MYTYMER_H
