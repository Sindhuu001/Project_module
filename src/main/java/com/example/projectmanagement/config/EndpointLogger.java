// package com.example.projectmanagement.config;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.method.HandlerMethod;
// import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
// import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

// import java.util.Map;
// import java.util.stream.Collectors;

// @Configuration
// public class EndpointLogger {

//     private static final Logger logger = LoggerFactory.getLogger(EndpointLogger.class);

//     @Bean
//     public CommandLineRunner logAllEndpoints(
//             @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mapping) {
//         return args -> {
//             Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
            
//             logger.info("\n========== ALL REGISTERED ENDPOINTS ==========");
//             logger.info(String.format("%-10s | %-30s | %s", "METHOD", "CONTROLLER", "PATH"));
//             logger.info("------------------------------------------------------------");
            
//             map.entrySet().stream()
//                 .sorted((e1, e2) -> {
//                     String path1 = getPath(e1.getKey());
//                     String path2 = getPath(e2.getKey());
//                     return path1.compareTo(path2);
//                 })
//                 .forEach(entry -> {
//                     RequestMappingInfo info = entry.getKey();
//                     HandlerMethod method = entry.getValue();
                    
//                     String methods = info.getMethodsCondition().getMethods().stream()
//                         .map(m -> m.name())
//                         .collect(Collectors.joining(","));
                    
//                     if (methods.isEmpty()) {
//                         methods = "ALL";
//                     }
                    
//                     String path = getPath(info);
//                     String controller = method.getBeanType().getSimpleName();
                    
//                     logger.info(String.format("%-10s | %-30s | %s", methods, controller, path));
//                 });
            
//             logger.info("==============================================\n");
//         };
//     }
    
//     private String getPath(RequestMappingInfo info) {
//         if (info.getPathPatternsCondition() != null) {
//             return info.getPathPatternsCondition().getPatterns().stream()
//                 .map(p -> p.getPatternString())
//                 .collect(Collectors.joining(", "));
//         } else if (info.getPatternsCondition() != null) {
//             return String.join(", ", info.getPatternsCondition().getPatterns());
//         }
//         return "";
//     }
// }
