//
// Created by spl211 on 04/01/2022.
//

#ifndef BOOST_ECHO_CLIENT_READFROMSOCKET_H
#define BOOST_ECHO_CLIENT_READFROMSOCKET_H
#include "connectionHandler.h";

class readFromSocket {
public:
    readFromSocket(ConnectionHandler& connectionHandler);
    void run();
    void createOP(std::string* stringToOp);
};


#endif //BOOST_ECHO_CLIENT_READFROMSOCKET_H
