#include "mytimer.h"
#include <QtCore>
#include <QDebug>
MyTimer::MyTimer()
{
    timer = new QTimer(this);
    connect(timer,SIGNAL(timeout()), this, SLOT(timerSlot()));

    timer->start(10000);

}

MyTimer::~MyTimer()
{
}

void MyTimer::timerSlot(){
    d.checkFlag();
    if( d.getStatus())
    {
        d.resetFlag();
        d.doDownload();
    }

}

