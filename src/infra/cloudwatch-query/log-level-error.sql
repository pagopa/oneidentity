fields @timestamp, @message
| filter @message like /ERROR/
| parse @message "[*] *" as thread, exceptionType
| sort @timestamp desc
| limit 20