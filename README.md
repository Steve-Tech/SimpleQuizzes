# SimpleQuizzes
A simple and lightweight quiz plugin.
## Config
```yaml
# Config File for SimpleQuizzes
delay: 30 # How long to wait between questions in seconds
check-chat: true # Enable responding from chat
messages: # %question%, %answer% and %player% placeholders are supported here.
  prefix: '&4[&2SimpleQuizzes&4]&r '
  new-question: '&6New Question: &b&l%question%'
  tip: "&eType &n'/quiz <answer>'&e to answer"
  answer: '&6The answer was &b&l%answer%&6.'
  correct-broadcast: '&a&l%player%&a has Answered Correctly! &6The answer was &b&l%answer%&6.'
  incorrect: '&4Wrong answer try again.'
  late: '&cSorry too late.'
questions:
  - question: "Whats 1+1?"
    answer: '2'
  - question: "Where is Microsoft's Headquarters?"
    answer: 'Redmond Washington'
rewards: # Commands to run (as console) when player gets an answer correct
  - 'give %player% diamond 1'
```
## Commands
- Quiz:
  - Aliases: answer
  - Description: Answer a quiz
  - Usage: /\<command\> \<answer\>
  - Permission: SimpleQuizzes.answer
  - Default: true
- SimpleQuizzes:
  - Description: Admin functions for SimpleQuizzes
  - Usage: /\<command\> \[reload | start | stop\]
  - Permission: SimpleQuizzes.reload
  - Default: false
