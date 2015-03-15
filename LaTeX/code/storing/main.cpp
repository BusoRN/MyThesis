#include <QCoreApplication>
#include "downloader.h"
#include "mytimer.h"
#include <QDebug>

int main(int argc, char *argv[])
{
    QCoreApplication a(argc, argv);
    MyTimer t;
    return a.exec();
}
