#include "downloader.h"
const QString FLAG_ON = " True ";

Downloader::Downloader(QObject *parent) :
    QObject(parent)
{
}

void Downloader::doDownload()
{
    manager = new QNetworkAccessManager(this);

    connect(manager, SIGNAL(finished(QNetworkReply*)),
            this, SLOT(replyFinished(QNetworkReply*)));

    manager->get(QNetworkRequest(QUrl("http://planar-contact-601.appspot.com/video/view")));
}

void Downloader::replyFinished (QNetworkReply *reply)
{
    if(reply->error())
    {
        qDebug() << "ERROR!";
        qDebug() << reply->errorString();
    }
    else
    {
        qDebug() << reply->header(QNetworkRequest::ContentTypeHeader).toString();
        qDebug() << reply->header(QNetworkRequest::LastModifiedHeader).toDateTime().toString();
        qDebug() << reply->header(QNetworkRequest::ContentLengthHeader).toULongLong();
        qDebug() << reply->attribute(QNetworkRequest::HttpStatusCodeAttribute).toInt();
        qDebug() << reply->attribute(QNetworkRequest::HttpReasonPhraseAttribute).toString();

        data = QDateTime::currentDateTime();
        name = "C:/Video/"+data.toString("yyyy-MM-dd-HH-mmss")+".mpeg";

        QFile *file = new QFile(name);
        if(file->open(QFile::Append))
        {
            file->write(reply->readAll());
            file->flush();
            file->close();
        }
        delete file;
    }

    reply->deleteLater();
}


void Downloader::checkStatus(QNetworkReply *replay)
{
    QString result(replay->readAll());
    int compare = QString::compare(result, FLAG_ON);
    if(compare == 0)
    {
        status = true;
    }
    else
    {
        status = false;
    }
    qDebug() << status;
    flag_ready = true;

}
void Downloader::checkFlag()
{
    manager = new QNetworkAccessManager(this);
    QNetworkReply *reply = manager->get(QNetworkRequest(QUrl("http://planar-contact-601.appspot.com/electrovalves/video")));
    QEventLoop loop;
    connect(reply, SIGNAL(finished()), &loop, SLOT(quit()));

    loop.exec();
    qDebug() << "I'm looking for a new video";
    QString result(reply->readAll());
    int compare = QString::compare(result, FLAG_ON);
    if(compare == 0)
    {
        status = true;
        qDebug() << "Found it";
    }
    else
    {
        status = false;
        qDebug() << "No new video available";
    }

}

bool Downloader::getStatus()
{
    return status;
}

void Downloader::resetFlag()
{
    status = false;
    manager2 = new QNetworkAccessManager(this);

    QUrl flag_url = QUrl("http://planar-contact-601.appspot.com/add/electrovalve");
    QByteArray postData;
    postData.append("name=video&status=off");
    manager2->post(QNetworkRequest(flag_url), postData);
}
