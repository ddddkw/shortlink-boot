package org.example.service;

import org.example.entity.ShortLinkDO;
import org.example.model.EventMessage;
import org.example.params.ShortLinkDelParam;

public interface LinkSeniorService {

    int addLink(ShortLinkDO shortLinkDO);

    int del(ShortLinkDelParam shortLinkDelParam);

    Boolean handlerAddShortLink(EventMessage eventMessage);

}
