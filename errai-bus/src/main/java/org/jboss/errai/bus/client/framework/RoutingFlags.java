package org.jboss.errai.bus.client.framework;

/**
 * Enumeration of flags that can be used when sending messages, to specify how they should be sent
 */
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
        }},

    Conversational {
        @Override
        public int flag() {
            return 1 << 2;
        }},

    FromRemote {
        @Override
        public int flag() {
            return 1 << 3;
        }}
    ;

    /**
     * Returns the integer representing the flag
     *
     * @return integer representation of the flag
     */
    public abstract int flag();
}
