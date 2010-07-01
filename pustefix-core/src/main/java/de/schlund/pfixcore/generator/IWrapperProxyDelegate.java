package de.schlund.pfixcore.generator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.schlund.pfixcore.generator.annotation.Caster;
import de.schlund.pfixcore.generator.annotation.Param;
import de.schlund.pfixcore.generator.annotation.PostCheck;
import de.schlund.pfixcore.generator.annotation.PreCheck;
import de.schlund.pfixcore.generator.annotation.Property;
import de.schlund.pfixcore.generator.casters.ToBoolean;
import de.schlund.pfixcore.generator.iwrpgen.BeanDescriptor;


public class IWrapperProxyDelegate extends IWrapperImpl {

    Class<?> modelClass;
    BeanDescriptor beanDesc;
    Object modelObject;
    
    public IWrapperProxyDelegate(Object modelObject) {
       this.modelClass = modelObject.getClass().getSuperclass();
       this.modelObject = modelObject;
    }
    
    @Override
    public void load(RequestData req) throws Exception {
        super.load(req);
        IWrapperParam[] params = gimmeAllParams();
        for(IWrapperParam param: params) {
            Method meth = beanDesc.getSetMethod(Character.toLowerCase(param.getName().charAt(0))+param.getName().substring(1));
            meth.invoke(modelObject, param.getValue());
        }
    }
    
    @Override
    protected void registerParams() {
        
        try {
        
        beanDesc = new BeanDescriptor(modelClass);
       
        Set<String> properties = beanDesc.getProperties();
        for (String property : properties) {
            
            String paramName = Character.toUpperCase(property.charAt(0)) + property.substring(1);
            boolean multiple = false;
            boolean optional = false;
            String type = null;
            
            Type targetType = beanDesc.getPropertyType(property);

            Class<?> targetClass = null;
            if (targetType instanceof Class<?>)
                targetClass = (Class<?>) targetType;
            else if (targetType instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) targetType).getRawType();
                if (rawType instanceof Class<?>)
                    targetClass = (Class<?>) rawType;
                else
                    throw new RuntimeException("Type not supported: " + targetType);
            } else
                throw new RuntimeException("Type not supported: " + targetType);

            if (targetClass.isArray()) {
                Class<?> compType = targetClass.getComponentType();
                multiple = true;
                type = compType.getName();
            } else if (List.class.isAssignableFrom(targetClass)) {
                Type argType = null;
                if (targetType instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) targetType;
                    Type[] argTypes = paramType.getActualTypeArguments();
                    if (argTypes.length == 1) {
                        argType = argTypes[0];
                        if (!(argType instanceof Class<?>)) throw new RuntimeException("Type not supported: " + argType);
                    } else
                        throw new RuntimeException("Type not supported: " + targetType);
                } else
                    throw new RuntimeException("Unparameterized List types aren't supported: " + targetType);
                
                List<?> list = null;
                if (!targetClass.isInterface()) {
                    try {
                        list = (List<?>) targetClass.newInstance();
                    } catch (Exception x) {}
                }
                if (list == null && !targetClass.isAssignableFrom(ArrayList.class)) {
                    throw new RuntimeException("Can't create instance of class '" + targetClass.getName() + "'.");
                }
                multiple = true;
                type = ((Class<?>) argType).getName();
            } else {
                if (targetClass.isPrimitive()) {
                    if (targetClass == boolean.class) targetClass = Boolean.class;
                    else if (targetClass == short.class) targetClass = Short.class;
                    else if (targetClass == int.class) targetClass = Integer.class;
                    else if (targetClass == long.class) targetClass = Long.class;
                    else if (targetClass == float.class) targetClass = Float.class;
                    else if (targetClass == double.class) targetClass = Double.class;
                    else if (targetClass == byte.class) targetClass = Byte.class;
                }
                type = targetClass.getName();
            }  
            
            
            Method getter = beanDesc.getGetMethod(property);
            Field field = beanDesc.getDirectAccessField(property);

            Param param = getter.getAnnotation(Param.class);
            if (param == null && field != null) param = field.getAnnotation(Param.class);
            if (param != null && !param.mandatory()) optional = true;
          
            boolean trim = true;
            if (param != null) trim = param.trim();
            
            IWrapperParam iwrapperParam = new IWrapperParam(paramName, multiple, optional, null, type, trim);
            
            String misscode = null;
            if (param != null) misscode = param.missingscode();
            if(misscode != null && !optional) iwrapperParam.setCustomSCode(misscode);

            params.put(paramName, iwrapperParam);
            
            Caster caster = getter.getAnnotation(Caster.class);
            if (caster == null && field != null) caster = field.getAnnotation(Caster.class);
            if (caster != null) {
               
                Class<? extends IWrapperParamCaster> casterType = caster.type();
                IWrapperParamCaster paramCaster = casterType.newInstance();
                iwrapperParam.setParamCaster(paramCaster);
                Property[] casterProps = caster.properties();
                if (casterProps != null) {
                    for (Property casterProp : casterProps) {
                      
                        String setterName = "set" + Character.toLowerCase(casterProp.name().charAt(0)) + casterProp.name().substring(1);
                        Method setterMeth = paramCaster.getClass().getMethod(setterName, String.class);
                        setterMeth.invoke(paramCaster, casterProp.value());
                    }
                }
            } else {
                //TODO: predefined casters
                if(type.equals("java.lang.Boolean")) {
                    IWrapperParamCaster paramCaster = new ToBoolean();
                    iwrapperParam.setParamCaster(paramCaster);
                }
            }
            

            PreCheck preCheck = getter.getAnnotation(PreCheck.class);
            if (preCheck == null && field != null) preCheck = field.getAnnotation(PreCheck.class);
            if (preCheck != null) {
             
                Class<? extends IWrapperParamPreCheck> preCheckType = preCheck.type();
                IWrapperParamPreCheck paramPreCheck = preCheckType.newInstance();
                iwrapperParam.addPreChecker(paramPreCheck);
                Property[] preCheckProps = preCheck.properties();
                if (preCheckProps != null) {
                    for (Property preCheckProp : preCheckProps) {
                                                
                        String setterName = "set" + Character.toLowerCase(preCheckProp.name().charAt(0)) + preCheckProp.name().substring(1);
                        Method setterMeth = paramPreCheck.getClass().getMethod(setterName, String.class);
                        setterMeth.invoke(paramPreCheck, preCheckProp.value());
                    }
                }
            }

            PostCheck postCheck = getter.getAnnotation(PostCheck.class);
            if (postCheck == null && field != null) postCheck = field.getAnnotation(PostCheck.class);
            if (postCheck != null) {
                
                Class<? extends IWrapperParamPostCheck> postCheckType = postCheck.type();
                IWrapperParamPostCheck paramPostCheck = postCheckType.newInstance();
                iwrapperParam.addPostChecker(paramPostCheck);
                Property[] postCheckProps = postCheck.properties();
                if (postCheckProps != null) {
                    for (Property postCheckProp : postCheckProps) {
                        
                        String setterName = "set" + Character.toLowerCase(postCheckProp.name().charAt(0)) + postCheckProp.name().substring(1);
                        Method setterMeth = paramPostCheck.getClass().getMethod(setterName, String.class);
                        setterMeth.invoke(paramPostCheck, postCheckProp.value());
                    }
                }
            }

        }

        super.registerParams();
        
        } catch(Exception x) {
            throw new RuntimeException("Error registering parameters", x);
        }
    }
    
}
