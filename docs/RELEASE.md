# Release Management

Release management has been set-up using Travis CI and Sonatype Nexus (= Maven Central)

## Continuous Integration

Travis CI is building all branches on commit hook.

## What modules get deployed

Every module is enabled by default. If you want to change this, please provide the property

     <skipNexusStagingDeployMojo>true</skipNexusStagingDeployMojo>
   
inside your `pom.xml`.

## Trigger release creation

We use gitflow for development (see [A successful git branching model](http://nvie.com/posts/a-successful-git-branching-model/) 
an introduction). You could use gitflow with native git
commands, but then you would have to change the versions in the poms manually. Therefore we use the 
[mvn gitflow plugin](https://github.com/aleksandr-m/gitflow-maven-plugin), which handles this and other things nicely.

You can build a release with:

	./mvnw -B gitflow:release-start gitflow:release-finish
	
This will update the versions in the `pom.xml`s accordingly and push the release tag to the `master` branch
and update the `develop` branch for the new development version.

## Trigger a deploy

Travis will deploy the results of the build if and only if:
- the build is stable
- the branch matches x.y.z, where x, y, z are numbers
- the commit contains a git tag

## SNAPSHOT vs. STABLE

If you deploy a SNAPSHOT version, the artifact ends up in the snapshot repository. If you want a release
in the Maven Central Repository, make sure to create a STABLE version (e.g. 2.1.3) and deploy it. Don't 
forget to close the release repository in the OSS Nexus.

## References

* https://www.phillip-kruger.com/post/continuous_integration_to_maven_central/ (primary)
* https://docs.travis-ci.com/user/deployment
* https://blog.travis-ci.com/2017-03-30-deploy-maven-travis-ci-packagecloud/


      
