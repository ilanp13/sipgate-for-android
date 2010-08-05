NDKBUILD=$(NDK_DIR)"/ndk-build"
PROJPATH=$(shell pwd)

setup: ndk
	test -d gen || mkdir gen

ndk:
	test -d "$(PROJPATH)/jni" || ( echo 'sure you are in the right project dir?' && false )
	test -d $(NDK_DIR) || ( echo 'NDK_DIR env var not set!' && false )
	test -x $(NDKBUILD) || false
	$(NDKBUILD) APP_PROJECT_PATH=$(PROJPATH)

	
