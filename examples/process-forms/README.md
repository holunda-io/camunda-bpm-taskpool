# Example Process Forms (Angular 6)

## Build

    docker build -t process-forms-build .

## Run (with watch)

    docker run -it --net=host \
     -v `pwd`/src:/opt/build/src \
     -v `pwd`/src-gen:/opt/build/src-gen \
     process-forms-build \
     npm start
