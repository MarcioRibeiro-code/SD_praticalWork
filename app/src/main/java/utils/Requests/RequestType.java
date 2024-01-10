package utils.Requests;

public enum RequestType {
    REGISTER, LOGIN, CREATE_CHANNEL,
    JOIN_CHANNEL, REQUEST_TASK, APPROVE_TASK,
    NOTIFY_INCIDENT, GET_NOTIFICATIONS, GET_TASKS, GET_CHANNELS,
    GET_JOINED_CHANNELS, GET_JOINABLE_CHANNELS,
    SEND_MESSAGE_TO_CHANNEL, SEND_MESSAGE_TO_USER, GET_USERS, GET_DIRECT_MESSAGE, FEEDBACK, GET_INBOX
}


// TODO : Created Register, Login , Create_Channel, Join_Channel, GET_Channels, GetJoinedChannels, GET Joinable channels,
// Send mensage to channel (Lado cliente), implementar notificações e tasks (lado servidor e cliente)