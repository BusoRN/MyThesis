#-------------------------------------------------
#
# Project created by QtCreator 2015-02-17T21:56:37
#
#-------------------------------------------------

QT       += core
QT       += network

QT       -= gui

#RC_FILE = myapp.rc

TARGET = VideoStoring
CONFIG   += console
CONFIG   -= app_bundle

TEMPLATE = app


SOURCES += main.cpp \
    downloader.cpp \
    mytimer.cpp

HEADERS += \
    downloader.h \
    mytimer.h
