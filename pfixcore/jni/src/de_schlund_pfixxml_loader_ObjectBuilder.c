#include "de_schlund_pfixxml_loader_ObjectBuilder.h"
#include <jni.h>

JNIEXPORT jobject JNICALL Java_de_schlund_pfixxml_loader_ObjectBuilder_allocateObject
  (JNIEnv *env, jobject obj, jclass clazz) {
 
 	 return (*env)->AllocObject(env,clazz);
  
}

JNIEXPORT void JNICALL Java_de_schlund_pfixxml_loader_ObjectBuilder_setObjectField
  (JNIEnv *env, jobject obj, jobject src, jstring name, jstring sign, jobject value) {
  
  	const char *fldName=(*env)->GetStringUTFChars(env,name,0);
  	const char *fldSign=(*env)->GetStringUTFChars(env,sign,0);
	jclass clazz=(*env)->GetObjectClass(env,src);
	jfieldID id=(*env)->GetFieldID(env,clazz,fldName,fldSign);
	(*env)->SetObjectField(env,src,id,value);
	(*env)->ReleaseStringUTFChars(env,name,fldName);
	(*env)->ReleaseStringUTFChars(env,sign,fldSign);
	return;
  
}

JNIEXPORT void JNICALL Java_de_schlund_pfixxml_loader_ObjectBuilder_setBooleanField
  (JNIEnv *env, jobject obj, jobject src, jstring name, jstring sign, jboolean value) {

    const char *fldName=(*env)->GetStringUTFChars(env,name,0);
  	const char *fldSign=(*env)->GetStringUTFChars(env,sign,0);
	jclass clazz=(*env)->GetObjectClass(env,src);
	jfieldID id=(*env)->GetFieldID(env,clazz,fldName,fldSign);
	(*env)->SetBooleanField(env,src,id,value);
	(*env)->ReleaseStringUTFChars(env,name,fldName);
	(*env)->ReleaseStringUTFChars(env,sign,fldSign);
	return;
  
}

JNIEXPORT void JNICALL Java_de_schlund_pfixxml_loader_ObjectBuilder_setByteField
  (JNIEnv *env, jobject obj, jobject src, jstring name, jstring sign, jbyte value) {

    const char *fldName=(*env)->GetStringUTFChars(env,name,0);
  	const char *fldSign=(*env)->GetStringUTFChars(env,sign,0);
	jclass clazz=(*env)->GetObjectClass(env,src);
	jfieldID id=(*env)->GetFieldID(env,clazz,fldName,fldSign);
	(*env)->SetByteField(env,src,id,value);
	(*env)->ReleaseStringUTFChars(env,name,fldName);
	(*env)->ReleaseStringUTFChars(env,sign,fldSign);
	return;
  
}

JNIEXPORT void JNICALL Java_de_schlund_pfixxml_loader_ObjectBuilder_setCharField
  (JNIEnv *env, jobject obj, jobject src, jstring name, jstring sign, jchar value) {

    const char *fldName=(*env)->GetStringUTFChars(env,name,0);
  	const char *fldSign=(*env)->GetStringUTFChars(env,sign,0);
	jclass clazz=(*env)->GetObjectClass(env,src);
	jfieldID id=(*env)->GetFieldID(env,clazz,fldName,fldSign);
	(*env)->SetCharField(env,src,id,value);
	(*env)->ReleaseStringUTFChars(env,name,fldName);
	(*env)->ReleaseStringUTFChars(env,sign,fldSign);
	return;
  
}

JNIEXPORT void JNICALL Java_de_schlund_pfixxml_loader_ObjectBuilder_setShortField
  (JNIEnv *env, jobject obj, jobject src, jstring name, jstring sign, jshort value) {

    const char *fldName=(*env)->GetStringUTFChars(env,name,0);
  	const char *fldSign=(*env)->GetStringUTFChars(env,sign,0);
	jclass clazz=(*env)->GetObjectClass(env,src);
	jfieldID id=(*env)->GetFieldID(env,clazz,fldName,fldSign);
	(*env)->SetShortField(env,src,id,value);
	(*env)->ReleaseStringUTFChars(env,name,fldName);
	(*env)->ReleaseStringUTFChars(env,sign,fldSign);
	return;
  
}

JNIEXPORT void JNICALL Java_de_schlund_pfixxml_loader_ObjectBuilder_setIntField
  (JNIEnv *env, jobject obj, jobject src, jstring name, jstring sign, jint value) {

    const char *fldName=(*env)->GetStringUTFChars(env,name,0);
  	const char *fldSign=(*env)->GetStringUTFChars(env,sign,0);
	jclass clazz=(*env)->GetObjectClass(env,src);
	jfieldID id=(*env)->GetFieldID(env,clazz,fldName,fldSign);
	(*env)->SetIntField(env,src,id,value);
	(*env)->ReleaseStringUTFChars(env,name,fldName);
	(*env)->ReleaseStringUTFChars(env,sign,fldSign);
	return;
  
}

JNIEXPORT void JNICALL Java_de_schlund_pfixxml_loader_ObjectBuilder_setLongField
  (JNIEnv *env, jobject obj, jobject src, jstring name, jstring sign, jlong value) {

    const char *fldName=(*env)->GetStringUTFChars(env,name,0);
  	const char *fldSign=(*env)->GetStringUTFChars(env,sign,0);
	jclass clazz=(*env)->GetObjectClass(env,src);
	jfieldID id=(*env)->GetFieldID(env,clazz,fldName,fldSign);
	(*env)->SetLongField(env,src,id,value);
	(*env)->ReleaseStringUTFChars(env,name,fldName);
	(*env)->ReleaseStringUTFChars(env,sign,fldSign);
	return;
  
}

JNIEXPORT void JNICALL Java_de_schlund_pfixxml_loader_ObjectBuilder_setFloatField
  (JNIEnv *env, jobject obj, jobject src, jstring name, jstring sign, jfloat value) {

    const char *fldName=(*env)->GetStringUTFChars(env,name,0);
  	const char *fldSign=(*env)->GetStringUTFChars(env,sign,0);
	jclass clazz=(*env)->GetObjectClass(env,src);
	jfieldID id=(*env)->GetFieldID(env,clazz,fldName,fldSign);
	(*env)->SetFloatField(env,src,id,value);
	(*env)->ReleaseStringUTFChars(env,name,fldName);
	(*env)->ReleaseStringUTFChars(env,sign,fldSign);
	return;
  
}

JNIEXPORT void JNICALL Java_de_schlund_pfixxml_loader_ObjectBuilder_setDoubleField
  (JNIEnv *env, jobject obj, jobject src, jstring name, jstring sign, jdouble value) {

    const char *fldName=(*env)->GetStringUTFChars(env,name,0);
  	const char *fldSign=(*env)->GetStringUTFChars(env,sign,0);
	jclass clazz=(*env)->GetObjectClass(env,src);
	jfieldID id=(*env)->GetFieldID(env,clazz,fldName,fldSign);
	(*env)->SetDoubleField(env,src,id,value);
	(*env)->ReleaseStringUTFChars(env,name,fldName);
	(*env)->ReleaseStringUTFChars(env,sign,fldSign);
	return;
  
}

JNIEXPORT void JNICALL Java_de_schlund_pfixxml_loader_ObjectBuilder_setStaticObjectField
  (JNIEnv *env, jobject obj, jclass clazz, jstring name, jstring sign, jobject value) {

    const char *fldName=(*env)->GetStringUTFChars(env,name,0);
  	const char *fldSign=(*env)->GetStringUTFChars(env,sign,0);
	jfieldID id=(*env)->GetStaticFieldID(env,clazz,fldName,fldSign);
	(*env)->SetStaticObjectField(env,clazz,id,value);
	(*env)->ReleaseStringUTFChars(env,name,fldName);
	(*env)->ReleaseStringUTFChars(env,sign,fldSign);
	return;
  
}

JNIEXPORT void JNICALL Java_de_schlund_pfixxml_loader_ObjectBuilder_setStaticBooleanField
  (JNIEnv *env, jobject obj, jclass clazz, jstring name, jstring sign, jboolean value) {

    const char *fldName=(*env)->GetStringUTFChars(env,name,0);
  	const char *fldSign=(*env)->GetStringUTFChars(env,sign,0);
	jfieldID id=(*env)->GetStaticFieldID(env,clazz,fldName,fldSign);
	(*env)->SetStaticBooleanField(env,clazz,id,value);
	(*env)->ReleaseStringUTFChars(env,name,fldName);
	(*env)->ReleaseStringUTFChars(env,sign,fldSign);
	return;
  
}

JNIEXPORT void JNICALL Java_de_schlund_pfixxml_loader_ObjectBuilder_setStaticByteField
  (JNIEnv *env, jobject obj, jclass clazz, jstring name, jstring sign, jbyte value) {

    const char *fldName=(*env)->GetStringUTFChars(env,name,0);
  	const char *fldSign=(*env)->GetStringUTFChars(env,sign,0);
	jfieldID id=(*env)->GetStaticFieldID(env,clazz,fldName,fldSign);
	(*env)->SetStaticByteField(env,clazz,id,value);
	(*env)->ReleaseStringUTFChars(env,name,fldName);
	(*env)->ReleaseStringUTFChars(env,sign,fldSign);
	return;
  
}

JNIEXPORT void JNICALL Java_de_schlund_pfixxml_loader_ObjectBuilder_setStaticCharField
  (JNIEnv *env, jobject obj, jclass clazz, jstring name, jstring sign, jchar value) {

    const char *fldName=(*env)->GetStringUTFChars(env,name,0);
  	const char *fldSign=(*env)->GetStringUTFChars(env,sign,0);
	jfieldID id=(*env)->GetStaticFieldID(env,clazz,fldName,fldSign);
	(*env)->SetStaticCharField(env,clazz,id,value);
	(*env)->ReleaseStringUTFChars(env,name,fldName);
	(*env)->ReleaseStringUTFChars(env,sign,fldSign);
	return;
  
}

JNIEXPORT void JNICALL Java_de_schlund_pfixxml_loader_ObjectBuilder_setStaticShortField
  (JNIEnv *env, jobject obj, jclass clazz, jstring name, jstring sign, jshort value) {

    const char *fldName=(*env)->GetStringUTFChars(env,name,0);
  	const char *fldSign=(*env)->GetStringUTFChars(env,sign,0);
	jfieldID id=(*env)->GetStaticFieldID(env,clazz,fldName,fldSign);
	(*env)->SetStaticShortField(env,clazz,id,value);
	(*env)->ReleaseStringUTFChars(env,name,fldName);
	(*env)->ReleaseStringUTFChars(env,sign,fldSign);
	return;
  
}

JNIEXPORT void JNICALL Java_de_schlund_pfixxml_loader_ObjectBuilder_setStaticIntField
  (JNIEnv *env, jobject obj, jclass clazz, jstring name, jstring sign, jint value) {

    const char *fldName=(*env)->GetStringUTFChars(env,name,0);
  	const char *fldSign=(*env)->GetStringUTFChars(env,sign,0);
	jfieldID id=(*env)->GetStaticFieldID(env,clazz,fldName,fldSign);
	(*env)->SetStaticIntField(env,clazz,id,value);
	(*env)->ReleaseStringUTFChars(env,name,fldName);
	(*env)->ReleaseStringUTFChars(env,sign,fldSign);
	return;
  
}

JNIEXPORT void JNICALL Java_de_schlund_pfixxml_loader_ObjectBuilder_setStaticLongField
  (JNIEnv *env, jobject obj, jclass clazz, jstring name, jstring sign, jlong value) {

    const char *fldName=(*env)->GetStringUTFChars(env,name,0);
  	const char *fldSign=(*env)->GetStringUTFChars(env,sign,0);
	jfieldID id=(*env)->GetStaticFieldID(env,clazz,fldName,fldSign);
	(*env)->SetStaticLongField(env,clazz,id,value);
	(*env)->ReleaseStringUTFChars(env,name,fldName);
	(*env)->ReleaseStringUTFChars(env,sign,fldSign);
	return;
  
}

JNIEXPORT void JNICALL Java_de_schlund_pfixxml_loader_ObjectBuilder_setStaticFloatField
  (JNIEnv *env, jobject obj, jclass clazz, jstring name, jstring sign, jfloat value) {

    const char *fldName=(*env)->GetStringUTFChars(env,name,0);
  	const char *fldSign=(*env)->GetStringUTFChars(env,sign,0);
	jfieldID id=(*env)->GetStaticFieldID(env,clazz,fldName,fldSign);
	(*env)->SetStaticFloatField(env,clazz,id,value);
	(*env)->ReleaseStringUTFChars(env,name,fldName);
	(*env)->ReleaseStringUTFChars(env,sign,fldSign);
	return;
  
}

JNIEXPORT void JNICALL Java_de_schlund_pfixxml_loader_ObjectBuilder_setStaticDoubleField
  (JNIEnv *env, jobject obj, jclass clazz, jstring name, jstring sign, jdouble value) {

    const char *fldName=(*env)->GetStringUTFChars(env,name,0);
  	const char *fldSign=(*env)->GetStringUTFChars(env,sign,0);
	jfieldID id=(*env)->GetStaticFieldID(env,clazz,fldName,fldSign);
	(*env)->SetStaticDoubleField(env,clazz,id,value);
	(*env)->ReleaseStringUTFChars(env,name,fldName);
	(*env)->ReleaseStringUTFChars(env,sign,fldSign);
	return;
  
}





