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
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
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
    private DefaultMutableTreeNode rootNode;
    private Object root;

    private Stack<DefaultMutableTreeNode> stk = new Stack<DefaultMutableTreeNode>();
    private Map<Object, Runnable> deferred = new HashMap<Object, Runnable>();

    private static Map<Class, ValRenderer> renderers = new HashMap<Class, ValRenderer>();

    public ObjectExplorer() {
        setCellRenderer(new MonitorTreeCellRenderer());
        buildTree();

        addTreeExpansionListener(new TreeExpansionListener() {
            public void treeExpanded(TreeExpansionEvent event) {
                Runnable r = deferred.get(event.getPath().getLastPathComponent());
                if (r != null) r.run();
            }

            public void treeCollapsed(TreeExpansionEvent event) {
            }
        });
    }

    public void setRoot(Object root) {
        this.root = root;
    }

    public void buildTree() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) getModel().getRoot();
        node.setUserObject(new JLabel("", getSwIcon("database.png"), SwingConstants.LEFT));

        stk.clear();
        stk.push(rootNode = node);

        node.removeAllChildren();

        DefaultTreeModel model = (DefaultTreeModel) getModel();

        if (root != null) {
            node.setUserObject(new JLabel(root.getClass().getName() + " = " + root, getSwIcon("class.png"), SwingConstants.LEFT));
            renderFields(this, node, root.getClass(), root);
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

    public static void nestObject(final ObjectExplorer explorer, DefaultMutableTreeNode node, String field, Object v) {
        if (v == null) return;
        DefaultMutableTreeNode nNode = createIconEntry("class.png", field + " {" + v.getClass().getName() + "} = " + v);
        node.add(nNode);

        renderFields(explorer, nNode, v != null ? v.getClass() : Object.class, v);
    }

    public static void renderFields(final ObjectExplorer explorer, final DefaultMutableTreeNode node, final Class clazz, final Object v) {
        if (clazz == null) return;

        if (clazz.isPrimitive()) {
            renderField(explorer, node, "val", PrimitiveMarker.class, v);
            return;
        }

        final DefaultMutableTreeNode placeholder = new DefaultMutableTreeNode("<Please Wait ...>");
        node.add(placeholder);

        Runnable r = new Runnable() {
            public void run() {
                if (!node.isNodeChild(placeholder)) {
                    return;
                }

                node.remove(placeholder);

                if (clazz.getSuperclass() != Object.class) {
                    renderFields(explorer, node, clazz.getSuperclass(), v);
                }

                for (Field fld : clazz.getDeclaredFields()) {
                    if ((fld.getModifiers() & Modifier.STATIC) != 0) continue;
                    fld.setAccessible(true);
                    try {
                        renderField(explorer, node, fld.getName(), fld.getType(), fld.get(v));
                    }
                    catch (Throwable t) {
                        t.printStackTrace();
                        node.add(createIconEntry("field.png", fld.getName() + " = <UNKNOWN>"));
                    }
                }

                ((DefaultTreeModel) explorer.getModel()).reload(node);

                explorer.deferred.remove(node);
            }
        };

        if (node == explorer.rootNode) {
            r.run();
        } else {
            explorer.deferred.put(node, r);
        }
    }

    public static void renderField(ObjectExplorer explorer, DefaultMutableTreeNode node, String field, Class clazz, Object v) {
        if (clazz.isArray()) {
            clazz = ArrayMarker.class;
        }

        if (!renderers.containsKey(clazz)) {
            if (!_scanClassHeirarchy(clazz, clazz)) {
                clazz = Object.class;
            }
        }

        renderers.get(clazz).render(explorer, node, field, v);
    }


    public static boolean _scanClassHeirarchy(Class clazz, Class root) {
        if (clazz.isPrimitive()) {
            renderers.put(clazz, renderers.get(PrimitiveMarker.class));
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
            public void render(ObjectExplorer explorer, DefaultMutableTreeNode node, String name, Object val) {
                node.add(createIconEntry("field.png", fieldLabel(name, val)));
            }
        });

        renderers.put(ArrayMarker.class, new ValRenderer() {
            public void render(ObjectExplorer explorer, DefaultMutableTreeNode node, String name, Object val) {
                DefaultMutableTreeNode arr = createIconEntry("field.png", fieldLabel(name, val));
                node.add(arr);

                int length = Array.getLength(val);
                //  Class type = val.getClass().getComponentType();
                Object o;
                for (int i = 0; i < length; i++) {
                    o = Array.get(val, i);
                    nestObject(explorer, arr, String.valueOf(i), o);
                }
            }
        });

        renderers.put(Collection.class, new ValRenderer() {
            public void render(ObjectExplorer explorer, DefaultMutableTreeNode node, String name, Object val) {
                DefaultMutableTreeNode arr = createIconEntry("field.png", fieldLabel(name, val));
                node.add(arr);

                int i = 0;
                for (Object o : (Collection) val) {
                    nestObject(explorer, arr, String.valueOf(i++), o);
                    //  explorer.renderFields(arr, o != null ? o.getClass() : Object.class, o);
                }
            }
        });

        renderers.put(Map.class, new ValRenderer() {
            public void render(ObjectExplorer explorer, DefaultMutableTreeNode node, String name, Object val) {
                DefaultMutableTreeNode arr = createIconEntry("field.png", fieldLabel(name, val));
                node.add(arr);

                //noinspection unchecked
                for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) val).entrySet()) {
                    nestObject(explorer, arr, String.valueOf(entry.getKey()), entry);
                }
            }
        });

        renderers.put(Object.class, new ValRenderer() {
            public void render(ObjectExplorer explorer, DefaultMutableTreeNode node, String name, Object val) {
                nestObject(explorer, node, name, val);
            }
        });

        ValRenderer boxedPrimRenderer = new ValRenderer() {
            public void render(ObjectExplorer explorer, DefaultMutableTreeNode node, String name, Object val) {

                node.add(createIconEntry("field.png", fieldLabel(name, val)));
            }
        };

        renderers.put(Integer.class, boxedPrimRenderer);
        renderers.put(Long.class, boxedPrimRenderer);
        renderers.put(Character.class, boxedPrimRenderer);
        renderers.put(Byte.class, boxedPrimRenderer);
        renderers.put(Short.class, boxedPrimRenderer);
        renderers.put(Double.class, boxedPrimRenderer);
        renderers.put(Boolean.class, boxedPrimRenderer);
        renderers.put(Float.class, boxedPrimRenderer);

        renderers.put(PrimitiveMarker.class, new ValRenderer() {
            public void render(ObjectExplorer explorer, DefaultMutableTreeNode node, String name, Object val) {
                node.add(createIconEntry("field.png", fieldLabelPrimitive(name, val)));
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

        return name + " {" + friendlyClassName(ParseTools.unboxPrimitive(c), v) + "} = " + friendlyValue(v);
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
        } else if (cls == Character.class) {
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
