#ifndef DOWNLOADER_H
#define DOWNLOADER_H

#include <QObject>
#include <QNetworkAccessManager>
#include <QNetworkRequest>
#include <QNetworkReply>
#include <QUrl>
#include <QDateTime>
#include <QFile>
#include <QDebug>
#include <QEventLoop>

class Downloader : public QObject
{
    Q_OBJECT
public:
    explicit Downloader(QObject *parent = 0);

    void doDownload();
    void checkFlag();
    bool getStatus();
    void resetFlag();

signals:

public slots:
    void replyFinished (QNetworkReply *reply);
    void checkStatus(QNetworkReply *replay);

private:
   QNetworkAccessManager *manager;
   QNetworkAccessManager *manager2;
   QDateTime data;
   QString name;
   bool flag_ready;
   bool status;


};

#endif // DOWNLOADER_H
