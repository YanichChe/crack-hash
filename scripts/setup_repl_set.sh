#!/bin/bash

mongosh --username admin --password password --authenticationDatabase admin --host mongo1:27017 --eval '
rs.initiate({
    _id: "rs0",
    members: [
        { _id: 0, host: "mongo1:27017" },
        { _id: 1, host: "mongo2:27017" },
        { _id: 2, host: "mongo3:27017" }
    ]
});
rs.status();'
