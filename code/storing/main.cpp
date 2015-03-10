//#include <QCoreApplication>
//#include <QTimer>
//#include "downloader.h"
//#include <QDebug>

//int main(int argc, char *argv[])
//{
//    QCoreApplication a(argc, argv);

//    QDateTime data;
//    data = QDateTime::currentDateTime();
//    QString name = "./"+data.toString("yyyy-MM-dd-HH:mm:ss")+".mp4";
//    Downloader d(0,name);
//    d.doDownload();
////    for(;;)
////    {
////        qDebug() << "Ciao";
////        if(d.isTime())
////        {
////            d.sendFlag();
////            d.doDownload();
////        }
////    }
//    return a.exec();
//}
#include <QCoreApplication>
#include "downloader.h"
#include "mytimer.h"
#include <QDebug>
#include <windows.h> // for Sleep


int main(int argc, char *argv[])
{
    QCoreApplication a(argc, argv);


    MyTimer t;

    //bool dummy = d.isTime();
//    while(1)
//    {
//        d.checkFlag();


//        if( d.getStatus())
//        {
//            d.resetFlag();
//            d.doDownload();
//        }
//        Sleep(20000);
//    }




    return a.exec();
}
