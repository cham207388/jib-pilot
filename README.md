# Learning about jib

# Build image (without pushing)
./gradlew jib

# Build and push to Docker Hub (requires credentials)
./gradlew jib \
-PdockerHubUsername=baicham \
-PdockerHubPassword=your_password

# Or use environment variables
export DOCKER_HUB_USERNAME=baicham
export DOCKER_HUB_PASSWORD=your_password
./gradlew jib