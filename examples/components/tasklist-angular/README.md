# Tasklist (Angular 6)

## Build

    docker build -t tasklist-angular-build .

## Run (with watch)

    docker run -it --net=host \
     -v `pwd`/src:/opt/build/src \
     -v `pwd`/src-gen:/opt/build/src-gen \
     tasklist-angular-build \
     npm start
