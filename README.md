# CrackHash

Distributed system for cracking a hash codenamed CrackHash. Hash cracking is 
implemented through a simple enumeration of a dictionary generated based on the 
alphabet (brute-force).

## Logic:
* Within the system, there is a manager that accepts a request from the user containing 
the MD-5 hash of a certain word, as well as its maximum length.
* The manager processes the request: generates tasks in accordance with the specified number of workers (computing nodes) for enumeration of words composed of the alphabet passed to them. Then sends them to the workers for execution.
* Each worker accepts the task, enumerates words in a given range and calculates their hash. Finds words whose hash matches the specified one, and returns the result of the work to the manager through the queue.

## Quickstart

Clone project:

```bash
git clone https://github.com/YanichChe/crack-hash.git
```
## Running

### Docker
```bash
bash run_docker_compose.sh
```

### Manually testing
```bash
docker-compose \
-f docker-compose.mongo.yml \
-f docker-compose.rabbitmq.yml up
```

