# Taskpool Cockpit (Angular 6)

## Build

    docker build -t taskpool-cockpit-build .

## Run (with watch)

    docker run -it --net=host -v `pwd`/src:/opt/build/src -v `pwd`/src-gen:/opt/build/src-gen taskpool-cockpit-build npm start