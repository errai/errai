/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.tools.monitoring;

import org.mvel2.util.ParseTools;
import org.mvel2.util.StringAppender;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static org.jboss.errai.tools.monitoring.UiHelper.createIconEntry;
import static org.jboss.errai.tools.monitoring.UiHelper.getSwIcon;
import static org.mvel2.util.ParseTools.boxPrimitive;

public class ObjectExplorer extends JTree {
    private Object root;

    private Stack<DefaultMutableTreeNode> stk = new Stack<DefaultMutableTreeNode>();

    private static Map<Class, ValRenderer> renderers = new HashMap<Class, ValRenderer>();

    public ObjectExplorer() {
        setCellRenderer(new MonitorTreeCellRenderer());
        buildTree();
    }

    public void setRoot(Object root) {
        this.root = root;
    }

    public void buildTree() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) getModel().getRoot();
        node.setUserObject(new JLabel("", getSwIcon("database.png"), SwingConstants.LEFT));

        stk.push(node);

        node.removeAllChildren();

        DefaultTreeModel model = (DefaultTreeModel) getModel();

        if (root != null) {
            node.setUserObject(new JLabel(root.getClass().getName() + " = " + root, getSwIcon("class.png"), SwingConstants.LEFT));
            renderFields(node, root.getClass(), root);
        } else {

        }
        model.reload();
    }

    public void addNode(DefaultMutableTreeNode node) {
        stk.peek().add(node);
    }

    public void nestNode(DefaultMutableTreeNode node) {
        addNode(node);
        stk.push(node);
    }

    public void popNode() {
        stk.pop();
    }

    public void renderFields(DefaultMutableTreeNode node, Class clazz, Object v) {
        if (clazz == null) return;

        if (clazz.isPrimitive()) {
            renderField(this, "val", PrimitiveMarker.class, v);
        }

        if (clazz.getSuperclass() != Object.class) {
            renderFields(node, clazz.getSuperclass(), v);
        }

        for (Field fld : clazz.getDeclaredFields()) {
            if ((fld.getModifiers() & Modifier.STATIC) != 0) continue;
            fld.setAccessible(true);
            try {
                renderField(this, fld.getName(), fld.getType(), fld.get(v));
            }
            catch (Throwable t) {
                t.printStackTrace();
                node.add(createIconEntry("field.png", fld.getName() + " = <UNKNOWN>"));
            }
        }
    }

    public static void renderField(ObjectExplorer explorer, String field, Class clazz, Object v) {
        if (clazz.isArray()) {
            clazz = ArrayMarker.class;
        }

        //    Class boxed = boxPrimitive(clazz);
        if (!renderers.containsKey(clazz)) {
            _scanClassHeirarchy(clazz, clazz);
        }

        renderers.get(clazz).render(explorer, field, v);
    }

    public static boolean _scanClassHeirarchy(Class clazz, Class root) {
        if (clazz.isPrimitive()) {
            renderers.put(clazz, renderers.get(Object.class));
            return true;
        }

        do {
            if (renderers.containsKey(root)) {
                renderers.put(boxPrimitive(clazz), renderers.get(root));
                return true;
            }

            for (Class iface : root.getInterfaces()) {
                if (_scanClassHeirarchy(clazz, iface)) return true;
            }
        } while ((root = root.getSuperclass()) != null);
        return false;
    }

    static {
        renderers.put(CharSequence.class, new ValRenderer() {
            public void render(ObjectExplorer explorer, String name, Object val) {
                explorer.addNode(createIconEntry("field.png", fieldLabel(name, val)));
            }
        });

        renderers.put(ArrayMarker.class, new ValRenderer() {
            public void render(ObjectExplorer explorer, String name, Object val) {
                DefaultMutableTreeNode arr = createIconEntry("field.png", fieldLabel(name, val));
                explorer.nestNode(arr);

                int length = Array.getLength(val);
                Class type = val.getClass().getComponentType();
                Object o;
                for (int i = 0; i < length; i++) {
                    o = Array.get(val, i);
                    explorer.renderFields(arr, type, o);
                }

                explorer.popNode();
            }
        });

        renderers.put(Collection.class, new ValRenderer() {
            public void render(ObjectExplorer explorer, String name, Object val) {
                DefaultMutableTreeNode arr = createIconEntry("field.png", fieldLabel(name, val));
                explorer.nestNode(arr);

                for (Object o : (Collection) val) {
                    explorer.renderFields(arr, o != null ? o.getClass() : Object.class, o);
                }

                explorer.popNode();
            }
        });

        renderers.put(Map.class, new ValRenderer() {
            public void render(ObjectExplorer explorer, String name, Object val) {
                DefaultMutableTreeNode arr = createIconEntry("field.png", fieldLabel(name, val));
                explorer.nestNode(arr);

                //noinspection unchecked
                for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) val).entrySet()) {
                    explorer.renderFields(arr, entry.getClass(), entry);
                }

                explorer.popNode();
            }
        });

        renderers.put(Object.class, new ValRenderer() {
            public void render(ObjectExplorer explorer, String name, Object val) {
                explorer.addNode(createIconEntry("field.png", fieldLabel(name, val)));
            }
        });

        renderers.put(PrimitiveMarker.class, new ValRenderer() {
            public void render(ObjectExplorer explorer, String name, Object val) {
                explorer.addNode(createIconEntry("field.png", fieldLabelPrimitive(name, val)));
            }
        });
    }


    static String fieldLabel(String name, Object v) {
        if (v == null) {
            return name + " = null";
        }
        return name + " {" + friendlyClassName(v) + "@" + v.hashCode() + "} = " + friendlyValue(v);
    }

       static String fieldLabelPrimitive(String name, Object v) {
        if (v == null) {
            return name + " = null";
        }

        Class c = v.getClass();

        return name + " {" + friendlyClassName(ParseTools.unboxPrimitive(c), v) +  "} = " + friendlyValue(v);
    }

    static String friendlyClassName(Object v) {
        if (v == null) return "null";

        return friendlyClassName(v.getClass(), v);
    }

    static String friendlyClassName(Class cls, Object v) {
        if (cls.isPrimitive()) {
            if (cls == char[].class) return "char[" + Array.getLength(v) + "]";
            else if (cls == int[].class) return "int[" + Array.getLength(v) + "]";
            else if (cls == long[].class) return "long[" + Array.getLength(v) + "]";
            else if (cls == double[].class) return "double[" + Array.getLength(v) + "]";
            else if (cls == short[].class) return "short[" + Array.getLength(v) + "]";
            else if (cls == float[].class) return "float[" + Array.getLength(v) + "]";
            else if (cls == boolean[].class) return "boolean[" + Array.getLength(v) + "]";
            else if (cls == byte[].class) return "byte[" + Array.getLength(v) + "]";
        } else if (cls.isArray()) {
            return cls.getComponentType().getName() + "[" + Array.getLength(v) + "]";
        } else if (Collection.class.isAssignableFrom(cls)) {
            return cls.getName() + " [" + ((Collection) v).size() + "]";
        }
        return cls.getName();
    }

    static String friendlyValue(Object v) {
        if (v == null) return "null";

        Class cls = v.getClass();

        if (cls.isArray()) {
            Class comp = cls.getComponentType();

            if (comp == char.class) {
                return "\"" + (Array.getLength(v) < 50 ? new String((char[]) v) : new String((char[]) v, 0, 50) + "...") + "\"";
            } else {
                StringAppender appender = new StringAppender();

                int len = Array.getLength(v);
                appender.append("[");
                for (int i = 0; i < len && i < 25;) {
                    appender.append(String.valueOf(Array.get(v, i)));
                    if (++i < len && i < 25) appender.append(", ");
                }
                return appender.append("]").toString();
            }
        }
        else if (cls == Character.class) {
            return "'" + String.valueOf(v) + "'";
        }


        return "\"" + String.valueOf(v) + "\"";
    }

    static class ArrayMarker {
    }

    static class PrimitiveMarker {
    }

    public static void main(String[] args) {
        System.out.println(char[].class.getComponentType());
    }

}
