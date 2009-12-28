package org.jboss.errai.bus.client;

public enum RoutingFlags {
    NonGlobalRouting {
        @Override
        public int flag() {
            return 1;
        }},
    PriorityProcessing {
        @Override
        public int flag() {
            return 1 << 1;
        }}
    ;
    
    public abstract int flag();
}
