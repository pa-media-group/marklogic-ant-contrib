package com.marklogic.predicates;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import javax.annotation.Nullable;

public class XPathMatchingPredicate implements Predicate<Element> {
    private String xpath;

    private Optional<XPathContext> namespaces = Optional.absent();

    private XPathMatchingPredicate(String xpath) {
        Preconditions.checkNotNull(xpath);
        this.xpath = xpath;
    }

    private XPathMatchingPredicate(String xpath, XPathContext namespaces) {
        Preconditions.checkNotNull(xpath);
        this.xpath = xpath;
        this.namespaces = Optional.of(namespaces);
    }

    public boolean apply(@Nullable Element input) {
        if (input == null) {
            return false;
        }
        Nodes nodes = namespaces.isPresent() ? input.query(xpath, namespaces.get()) : input.query(xpath);
        return nodes != null && nodes.size() > 0;
    }

    public static Predicate<Element> match(String xpath) {
        return new XPathMatchingPredicate(xpath);
    }

    public static Predicate<Element> match(String xpath, XPathContext namespaces) {
        return new XPathMatchingPredicate(xpath, namespaces);
    }
}