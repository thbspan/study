package com.test.arthas.instrument.ognl;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Map;

import ognl.AbstractMemberAccess;

public class DefaultMemberAccess extends AbstractMemberAccess {
    private boolean allowPrivateAccess;
    private boolean allowProtectedAccess;
    private boolean allowPackageProtectedAccess;

    public DefaultMemberAccess(boolean allowAllAccess) {
        this(allowAllAccess, allowAllAccess, allowAllAccess);
    }

    public DefaultMemberAccess(boolean allowPrivateAccess, boolean allowProtectedAccess, boolean allowPackageProtectedAccess) {
        this.allowPrivateAccess = allowPrivateAccess;
        this.allowProtectedAccess = allowProtectedAccess;
        this.allowPackageProtectedAccess = allowPackageProtectedAccess;
    }

    @Override
    public Object setup(Map context, Object target, Member member, String propertyName) {
        Object result = null;
        if (isAccessible(context, target, member, propertyName)) {
            AccessibleObject accessible = (AccessibleObject) member;
            if (!accessible.isAccessible()) {
                result = Boolean.TRUE;
                accessible.setAccessible(true);
            }
        }
        return result;
    }

    @Override
    public void restore(Map context, Object target, Member member, String propertyName, Object state) {
        if (state instanceof Boolean) {
            ((AccessibleObject) member).setAccessible((Boolean) state);
        }
    }

    @Override
    public boolean isAccessible(Map context, Object target, Member member, String propertyName) {
        int modifiers = member.getModifiers();
        return Modifier.isPublic(modifiers)
                || (Modifier.isPrivate(modifiers) && allowPrivateAccess)
                || (Modifier.isProtected(modifiers) && allowProtectedAccess)
                || allowPackageProtectedAccess;
    }

    public void setAllowPrivateAccess(boolean allowPrivateAccess) {
        this.allowPrivateAccess = allowPrivateAccess;
    }

    public void setAllowProtectedAccess(boolean allowProtectedAccess) {
        this.allowProtectedAccess = allowProtectedAccess;
    }

    public void setAllowPackageProtectedAccess(boolean allowPackageProtectedAccess) {
        this.allowPackageProtectedAccess = allowPackageProtectedAccess;
    }
}
