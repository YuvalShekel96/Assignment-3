#include <stdlib.h>
#include <connectionHandler.h>
#include <thread>
#include<sstream>
#include <ctime>
#include <iomanip>
/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
bool terminate= false;
bool loggedIn=false;
typedef unsigned char BYTE;

void inputFromUser(ConnectionHandler* connectionHandler);
void updateString(std::string *pString);
void inputFromServer(ConnectionHandler* connectionHandler);

void decodeAnswerFromServer(std::string *basicString);

void shortToBytes(short op, char *string);

void addNullTerms(std::string *pString);

short bytesToShort(char *pString);

void deleteNullTerms(std::string *pString);

std::string getTimeStamp();

std::string decodeSTAT(std::basic_string<char, std::char_traits<char>, std::allocator<char>> basicString);

void addSlashes(std::string *pString);

int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);
    
    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
    std::thread keyThread(inputFromUser,&connectionHandler);
    std::thread serverThread(inputFromServer,&connectionHandler);
    serverThread.join();
    keyThread.join();
    return 0;
}

void inputFromServer(ConnectionHandler* connectionHandler){
    while (!terminate) {
        std::string answer;
        if (!connectionHandler->getLine(answer)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            terminate = true;
            break;
        }
        int len = answer.length();
        answer.resize(len - 1);
        decodeAnswerFromServer(&answer);
        std::cout << answer  <<  std::endl;
        if(answer == "ACK 2")
            loggedIn=true;
        if (answer == "bye") {
            std::cout << "Exiting...\n" << std::endl;
            terminate = true;
            break;
        }
    }
}
void decodeAnswerFromServer(std::string* s){
    std::string newS;
    std::stringstream ss;
    short op=bytesToShort(&(*s)[0]);
        if(op== 9) {
            std::string pubOrPM;
            char c = 1;
            if (s->c_str()[2] == c)
                pubOrPM = " Public ";
            else
                pubOrPM = " PM ";
            deleteNullTerms(s);
            newS = "NOTIFICATION" + pubOrPM +" "+ s->substr(3);
            *s = newS;
        }
        else if(op== 10) {
            short comOp = bytesToShort(&(*s)[2]);
            if ((comOp == 7) | (comOp == 8)) {
                std::string ans = decodeSTAT(s->substr(4));
                ss << "ACK " << comOp << ans;
                *s = ss.str();
            }
            else{
                ss << "ACK " << comOp<<s->substr(4);
                *s = ss.str();
            }
        }
        else {
            ss << "Error " << bytesToShort(&(*s)[2]);
            *s = ss.str();
        }

    }

std::string decodeSTAT(std::string basicString) {
    std::stringstream returedString;
    for(unsigned int i=0; i<basicString.length();i=i+2){
        short info= bytesToShort(&basicString[i]);
        returedString<<" "<<info;
    }
    return returedString.str();
}


void deleteNullTerms(std::string* s){
    unsigned int indexOfNextNull=0;
    while((indexOfNextNull=s->find('\0',indexOfNextNull+1)) < ((*s).length())) {
        (*s)[indexOfNextNull]= ' ';
    }
}



void inputFromUser(ConnectionHandler* connectionHandler){
    while(!terminate) {
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        int len = line.length();
        updateString(&line);
        if (!connectionHandler->sendLine(line)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            terminate = true;
            break;
        }
    }
}


void updateString(std::string* s){
    int indexOfEndCom=s->find(' ');
    std::string comString=s->substr(0,indexOfEndCom);
    if(comString== "REGISTER") {
        std::string newS = s->substr(indexOfEndCom - 1);
        short op=1;
        shortToBytes(op,&newS[0]);
        addNullTerms(&newS);
        *s = newS + '\0';
    }
    else if(comString== "LOGIN") {
        std::string newS = s->substr(indexOfEndCom - 1,sizeof (*s)-indexOfEndCom-1);
        short op=2;
        shortToBytes(op,&newS[0]);
        addNullTerms(&newS);
        char captcha = 1;
        if(newS.back() == '1') {
            newS.back() = captcha;
        }
        *s = newS;
    }
    else if(comString== "LOGOUT") {
        std::string newS="00";
        short op=3;
        shortToBytes(op,&newS[0]);
        *s=newS;
        if(loggedIn) {
            terminate = true;
            std::cout<<"we set terminate to true"<<std::endl;
        }
    }
    else if(comString== "FOLLOW") {
        std::string newS = s->substr(indexOfEndCom - 1);
        short op=4;
        shortToBytes(op,&newS[0]);
        if(newS[2]=='1')
            newS[2]=(char)1;
        else
            newS[2]=(char)0;
        *s = newS+'\0';
    }
    else if(comString== "POST") {
        std::string newS = s->substr(indexOfEndCom - 1);
        short op=5;
        shortToBytes(op,&newS[0]);
        *s = newS + '\0';
    }
    else if(comString== "PM") {
        std::string opString="00";
        std::string newS = s->substr(indexOfEndCom+1);
        short op=6;
        shortToBytes(op,&opString[0]);
        std::string timeStamp=getTimeStamp();
        std::string recipient=newS.substr(0,newS.find(' '));
        newS=newS.substr(newS.find(' ')+1);
        *s = opString+recipient+'\0'+newS+'\0'+timeStamp+'\0';
    }
    else if(comString== "LOGSTAT") {;
        std::string newS = "00";
        short op=7;
        shortToBytes(op,&newS[0]);
        *s = newS;
    }
    else if(comString== "STAT") {
        std::string newS = s->substr(indexOfEndCom - 1);
        short op=8;
        shortToBytes(op,&newS[0]);
        addSlashes(&newS);
        *s = newS + '\0';
    }
    else if(comString=="BLOCK"){
        std::string newS = s->substr(indexOfEndCom - 1);
        short op=12;
        shortToBytes(op,&newS[0]);
        *s = newS + '\0';
    }

}

void addSlashes(std::string *pString) {
    unsigned int indexOfNextNull=0;
    while((indexOfNextNull=pString->find(' ',indexOfNextNull+1)) < ((*pString).length())) {
        (*pString)[indexOfNextNull]= '|';
    }
}

std::string getTimeStamp() {
    std::time_t timer = std::time(0);
    std::tm *now = std::localtime(&timer);
    std::stringstream date_str_stream;
    date_str_stream << std::put_time(now, "%d-%m-%Y %H-%M");
    return date_str_stream.str();

}

void addNullTerms(std::string *pString) {
    unsigned int indexOfNextNull=0;
    while((indexOfNextNull=pString->find(' ',indexOfNextNull+1)) < ((*pString).length())) {
        (*pString)[indexOfNextNull]= '\0';
    }
}


void shortToBytes(short num, char * bytesArr)
{
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}
short bytesToShort(char* bytesArr)
{
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}