#include <sys/types.h>    // for socket
#include <sys/socket.h>    // for socket
#include <stdio.h>        // for printf
#include <stdlib.h>        // for exit
#include <string.h>        // for bzero
#include <arpa/inet.h>

#define SERVER_PORT    14000
#define LENGTH_OF_LISTEN_QUEUE 20
#define BUFFER_SIZE 1024
#define FILE_NAME_MAX_SIZE 512

char* localip = "222.204.248.136";

int exit_flag = 1;

int main(int argc, char **argv) {

    struct sockaddr_in server_addr;

    bzero(&server_addr, sizeof(server_addr));
    server_addr.sin_family = AF_INET;

    server_addr.sin_port = htons(SERVER_PORT);

    server_addr.sin_addr.s_addr = inet_addr(localip);

    int server_socket = socket(AF_INET, SOCK_STREAM, 0);

    if (server_socket < 0) {
        printf("Create Socket Failed!");
        exit(1);
    }

    int opt = 1;

    setsockopt(server_socket, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

    if (bind(server_socket, (struct sockaddr *) &server_addr, sizeof(server_addr))) {
        printf("Server Bind Port : %d Failed!",SERVER_PORT);
        exit(1);
    }

    if (listen(server_socket, LENGTH_OF_LISTEN_QUEUE)) {
        printf("Server Listen Failed!");
        exit(1);
    }


    struct sockaddr_in client_addr;
    socklen_t length = sizeof(client_addr);

    int len ;
    char buffer[BUFFER_SIZE];
    int client_socket;

    printf("Ready to be connected.\n");
    printf("IP: %s:%d\n", localip, SERVER_PORT);

ReadyLabel:
    client_socket = accept(server_socket, (struct sockaddr *) &client_addr, &length);

    printf("Message: Connect!\n");

    bzero(buffer, BUFFER_SIZE);

    while (1) {

        len = recv(client_socket, buffer, BUFFER_SIZE, 0);


        if (len > 0) {
            buffer[len] = '\0';
            printf("%s\n", buffer);
           //len = send(client_socket, "Received.\n", 10, 0);
            if (len < 0) {
                printf("Error: Send error!\n");
                break;
            }
        }
        else {
            printf("Error: Receive error!\n ");
            break;
        }

        if(!strcmp(buffer,"GetAll")) {
            char tp[11];
                int datatemp, datahumi, datax, datay, dataz, dataposi;
                datatemp = (rand() % 51);
                datahumi = (rand() % 101);
                datax = (rand() % 181);
                datay = (rand() % 181)+1;
                dataz = (rand() % 181)+2;
                dataposi = (rand() % 6);
            sprintf(tp, "data,%d,%d,%d,%d,%d,%d,\r\n", datatemp,datahumi,datax,datay,dataz,dataposi);
            len = send(client_socket, tp, strlen(tp), 0);
                printf("Data: %d,%d,%d,%d,%d,%d\r\n",datatemp,datahumi,datax,datay,dataz,dataposi);
        }
        else if(!strcmp(buffer,"Exit")) {
            printf("Meaasge: Exit Get Here\r\n");
            //len = send(client_socket, "Client is down\n",15, 0);
            break;
        }
        else if(!strcmp(buffer,"Down") ){
           printf("Meaasge: Server shut down.\r\n");
                len = send(client_socket, "Disconnect.\n",12,0);

           goto EndPoint;
        }
        else{
                len = send(client_socket,buffer,strlen(buffer),0);
                len = send(client_socket,"Received.\n",10,0);
        }
        bzero(buffer,strlen(buffer));

    }

    shutdown(client_socket, 2);
    printf("Message: Ready to restart.");
    goto ReadyLabel;


EndPoint:

    shutdown(server_socket,2);

    return 0;
}