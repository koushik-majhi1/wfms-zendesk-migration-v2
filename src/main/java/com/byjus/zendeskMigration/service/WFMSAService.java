//package com.byjus.zendeskMigration.service;
//
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.net.SocketException;
//import java.net.SocketTimeoutException;
//import java.util.concurrent.TimeUnit;
//
///**
// * @author koushik.majhi1@byjus.com
// * @created 05/07/2023 - 8:35 am
// */
//
//@Service
//public class WFMSAService {
//
//    private <T> String SaveItemsInWFMS(String endpoint, T item, String projectid) throws Exception {
//        return SaveItemsInWFMS(endpoint, item, projectid, "POST");
//    }
//
//    private <T> String SaveItemsInWFMS(String endpoint, T item, String projectid,String methodType) throws Exception {
//        return SaveItemsInWFMS(endpoint, item, projectid,methodType,0);
//    }
//
//    private static <T> String SaveItemsInWFMS(String endpoint, T item, String projectid, String methodType, int rerty) throws Exception {
//        try {
//
//            MediaType mediaType = MediaType.parse("application/json");
//            RequestBody body = RequestBody.create(mediaType, gson.toJson(item));
//
//            Request request = new Request.Builder()
//                    .url(Endpoint + endpoint)
//                    .method(methodType, body)
//                    .addHeader("content-type", "application/json")
//                    .addHeader("projectid", projectid)
//                    .addHeader("authority", "h-stage-apigateway.byjus.onl")
//                    .addHeader("accept", "application/json, text/plain, */*")
//                    .addHeader("accept-language", "en-GB,en-US;q=0.9,en;q=0.8")
//                    .addHeader("origin", "https://workflow-managment-system-staging.byjusweb.com")
//                    .addHeader("referer", "https://workflow-managment-system-staging.byjusweb.com/")
//                    .addHeader("sec-ch-ua", "\"Google Chrome\";v=\"113\", \"Chromium\";v=\"113\", \"Not-A.Brand\";v=\"24\"")
//                    .addHeader("sec-ch-ua-mobile", "?0")
//                    .addHeader("sec-ch-ua-platform", "\"macOS\"")
//                    .addHeader("sec-fetch-dest", "empty")
//                    .addHeader("sec-fetch-mode", "cors")
//                    .addHeader("sec-fetch-site", "cross-site")
//                    .addHeader("sessiontoken", SessionToken)
//                    .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36")
//                    .build();
//            //System.out.println(endpoint + "   " + i);
//            Response response = client.newCall(request).execute();
//            String json = response.body().string();
//            //System.out.println(response);
//            if (json.contains("Agent.Email is Blank") || json.contains("Agent Validation Failed") ) {
//                return json;
//            } else if (response.code() != 200 && response.code() != 201 && rerty < 5) {
//                return SaveItemsInWFMS(endpoint, item, projectid, methodType, ++rerty);
//            } else if (rerty >= 5) {
//                System.out.println("***************" + response.code() + "***************" + json);
//            }
//            return json;
//        } catch (SocketException ex) {
//            if (rerty < 5) {
//                return SaveItemsInWFMS(endpoint, item, projectid, methodType, ++rerty);
//            } else {
//                System.out.println(ex.toString() + ex.getStackTrace());
//                return ex.toString();
//            }
//        } catch (SocketTimeoutException ex) {
//            if (rerty < 5) {
//                return SaveItemsInWFMS(endpoint, item, projectid, methodType, ++rerty);
//            } else {
//                System.out.println(ex.toString() + ex.getStackTrace());
//                return ex.toString();
//            }
//        } catch (StreamResetException ex) {
//            client = new OkHttpClient().newBuilder()
//                                       .connectTimeout(50, TimeUnit.SECONDS)
//                                       .writeTimeout(50, TimeUnit.SECONDS)
//                                       .build();
//
//            if (rerty < 5) {
//                return SaveItemsInWFMS(endpoint, item, projectid, methodType, ++rerty);
//            } else {
//                System.out.println(ex.toString() + ex.getStackTrace());
//                return ex.toString();
//            }
//        } catch (IOException ex) {
//
//            if (rerty < 5) {
//                return SaveItemsInWFMS(endpoint, item, projectid, methodType, ++rerty);
//            } else {
//                System.out.println(ex.toString() + ex.getStackTrace());
//                return ex.toString();
//            }
//        } catch (Exception ex) {
//            System.out.println(ex.toString() + ex.getStackTrace());
//            throw ex;
//        }
//    }
//}
