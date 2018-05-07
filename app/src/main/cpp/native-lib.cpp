#include <jni.h>
#include <string>

#include <android/log.h>

#include <openssl/buffer.h>
#include <openssl/crypto.h>
#include <openssl/engine.h>
#include <openssl/err.h>

#define LOG_TAG "test"

#define  LOGD(...)        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

void logSslErr(const char* desc, const char* param)
{
      char lastError[2048]={0};
      int n = ERR_get_error();
      char szErr[1024];
      if(param){
        sprintf(lastError, "call %s failed. param:[%s] OpenSSL error:[%d] reason:[%s]", desc, param, n, ERR_error_string( n, szErr ));
      }else{
        sprintf(lastError, "call %s failed. OpenSSL error:[%d] reason:[%s]", desc, n, ERR_error_string( n, szErr ));
      }
      LOGD(lastError);
}

static void display_engine_list(void)
{
      ENGINE *h;
      int loop;

      h = ENGINE_get_first();
      loop = 0;
      LOGD("listing available engine types\n");
      while (h) {
        LOGD("engine %i, id = \"%s\", name = \"%s\"\n",
             loop++, ENGINE_get_id(h), ENGINE_get_name(h));
        h = ENGINE_get_next(h);
      }
      LOGD("end of list\n");
      /*
       * ENGINE_get_first() increases the struct_ref counter, so we must call
       * ENGINE_free() to decrease it again
       */
      ENGINE_free(h);
}

static int OpenSSL_Sign(EVP_PKEY *pPriKey,
                        unsigned char *data, int data_cb,
                        unsigned char* sign, unsigned int *psign_cb	)
{
      const EVP_MD *md = NULL;
      EVP_MD_CTX md_sign_ctx;
      int nRet = 0, n = 0;
      char szErr[1024];

      //根据密钥类型选择摘要算法
      switch (pPriKey->type) {
        case EVP_PKEY_EC:
          md = EVP_ecdsa();
          break;
        default:
          md = EVP_sha1();
          break;
      }

      //摘要上下文初始化
      if( !EVP_SignInit ( &md_sign_ctx, md ) ) {
        n  = ERR_get_error();
        ERR_error_string( n, szErr );
        fprintf( stderr, "OpenSSL_Sign: EVP_SignInit failed: \nopenssl return %d, %s\n", n, szErr );

        nRet = -1;
        goto sign_ret;
      }

      //签名所需的摘要计算，如果有多段数据，可以多次调用EVP_SignUpdate
      if( ! EVP_SignUpdate(&md_sign_ctx, data, data_cb) ) {
        n  = ERR_get_error();
        ERR_error_string( n, szErr );

        fprintf( stderr,"OpenSSL_Sign: EVP_SignUpdate failed: \nopenssl return %d, %s\n", n, szErr );

        nRet = -2;
        goto sign_ret;
      }

      //计算签名
      if( !EVP_SignFinal (&md_sign_ctx,
                          sign,
                          psign_cb,
                          pPriKey ) ) {
        n  = ERR_get_error();
        ERR_error_string( n, szErr );

        fprintf( stderr,"OpenSSL_Sign: EVP_SignFinal failed: \nopenssl return %d, %s\n", n, szErr );

        nRet = -3;
        goto sign_ret;
      }

      LOGD("OpenSSL_Sign success.");

      sign_ret:
      if( !EVP_MD_CTX_cleanup(&md_sign_ctx) ) {
        n  = ERR_get_error();
        ERR_error_string( n, szErr );
        fprintf( stderr,"OpenSSL_Sign: EVP_ctx_cleanup failed: \nopenssl return %d, %s\n", n, szErr );
      }

      return nRet;
}

static int OpenSSL_Verify(EVP_PKEY *pPubKey,
                          unsigned char *data, int data_cb,
                          unsigned char* sign, unsigned int sign_cb	)
{
      const EVP_MD *md = NULL;
      EVP_MD_CTX md_sign_ctx, md_verify_ctx;
      int nRet = 0, n = 0;
      char szErr[1024];

      //根据密钥类型选择摘要算法
      switch (pPubKey->type) {
        case EVP_PKEY_EC:
          md = EVP_ecdsa();
          break;
        default:
          md = EVP_sha1();
          break;
      }

      //摘要上下文初始化
      if( !EVP_VerifyInit( &md_verify_ctx, md ) ) {
        n  = ERR_get_error();
        ERR_error_string( n, szErr );

        fprintf( stderr, "OpenSSL_Verify: EVP_VerifyInit failed: \nopenssl return %d, %s\n", n, szErr );

        nRet = -4;
        goto verify_ret;
      }

      //验签所需的摘要计算，如果有多段数据，可以多次调用EVP_VerifyUpdate
      if( !EVP_VerifyUpdate(&md_verify_ctx, data, data_cb) ) {
        n  = ERR_get_error();
        ERR_error_string( n, szErr );

        fprintf( stderr, "OpenSSL_Verify: EVP_VerifyUpdate failed: \nopenssl return %d, %s\n", n, szErr );

        nRet = -5;
        goto verify_ret;
      }

      //验证签名
      if( !EVP_VerifyFinal(&md_verify_ctx, sign, sign_cb, pPubKey) ) {
        n  = ERR_get_error();
        ERR_error_string( n, szErr );

        fprintf( stderr, "OpenSSL_Verify: EVP_VerifyFinal failed: \nopenssl return %d, %s\n", n, szErr );

        nRet = -6;
        goto verify_ret;
      }

      LOGD("OpenSSL_Verify success.");

      verify_ret:
      if( !EVP_MD_CTX_cleanup(&md_verify_ctx) ) {
        n  = ERR_get_error();
        ERR_error_string( n, szErr );
        fprintf( stderr, "OpenSSL_Verify: EVP_ctx_cleanup failed: \nopenssl return %d, %s\n", n, szErr );
      }

      return nRet;
}

extern "C"
JNIEXPORT jstring
JNICALL
Java_nativ_ks_MainActivity_stringFromJNI(
    JNIEnv *env,
    jobject /* this */) {
      std::string hello = "Hello from C++";
      return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jint JNICALL
Java_nativ_ks_MainActivity_getKeyStoreInNative(JNIEnv *env,
                                               jclass type,
                                               jstring alias_) {
      const char *fullName = env->GetStringUTFChars(alias_, 0);
      int ret = 0;
      char *plain = "this is plain text.";
      unsigned char signature[256] = {0};
      unsigned int sigLen = 0;
      EVP_PKEY *priKey = NULL, *pubKey = NULL;

      ERR_load_ERR_strings();

      LOGD("getKeyStoreInNative. alias:%s",fullName);

      ENGINE * engine = ENGINE_by_id("keystore");
      if(!engine){

        LOGD("starting to load engine keystore...........");

        ENGINE_load_dynamic();

        engine = ENGINE_by_id("keystore");
        if(!engine){
          LOGE("can not find \"keystore\" ENGINE, return.");
          ret = -1;
          goto ERR;
        }

        if (!ENGINE_init(engine)){
          logSslErr("ENGINE_init", NULL);
          ret = -2;
          goto ERR;
        }

        if(!ENGINE_add(engine)){
          logSslErr("ENGINE_add", NULL);
          ret = -3;
          /*
           * fix: in Samsung S5:
           * call ENGINE_add failed.
           * OpenSSL error:[638025831] reason:[error:26078067:engine routines:ENGINE_LIST_ADD:conflicting engine id]
           * */
          // goto ERR;
        }

        LOGD("END load engine keystore...........");
      }else{
        LOGD("use default. already find keystore engine.");
      }

      display_engine_list();
      /*
       * D/test    (30870): engine 0, id = "dynamic", name = "Dynamic engine loading support"
         D/test    (30870): engine 1, id = "keystore", name = "Android keystore engine"
       * */

      priKey = ENGINE_load_private_key(engine,fullName,NULL,NULL);
      if (!priKey){
        logSslErr("ENGINE_load_private_key", NULL);
        ret = -4;
        goto ERR;
      }

      LOGD("ENGINE_load_private_key:%s success. addr:%p",fullName, priKey);

      pubKey = ENGINE_load_public_key(engine,fullName,NULL,NULL);
      if (!pubKey){
        logSslErr("ENGINE_load_public_key", NULL);
        ret = -5;
        goto ERR;
      }

      LOGD("ENGINE_load_public_key:%s success. addr:%x",fullName, pubKey);

      ret = OpenSSL_Sign(priKey,(unsigned char*)plain,strlen((const char*)plain),signature,&sigLen);
      LOGD("OpenSSL_Sign. ret:%d sigLen:%d",ret,sigLen);
      ret = OpenSSL_Verify(pubKey,(unsigned char*)plain,strlen(plain),signature,sigLen);
      LOGD("OpenSSL_Verify. ret:%d",ret);

  ERR:

      if(engine){
//        if (!ENGINE_remove(engine))
//          logSslErr("ENGINE_remove", NULL);
//        if (!ENGINE_free(engine))
//          logSslErr("ENGINE_free", NULL);
      }

      env->ReleaseStringUTFChars(alias_, fullName);
      return ret;
}