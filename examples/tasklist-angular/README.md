# Tasklist (Angular 6)

## Build 

    docker build -t tasklist-angular-build .

## Run (with watch)

    docker run -it --net=host -v `pwd`/src:/opt/build/src tasklist-angular-build npm start
