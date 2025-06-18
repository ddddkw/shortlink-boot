package org.example.service;

import org.example.entity.ShortLinkDO;
import org.example.model.EventMessage;
import org.example.params.ShortLinkDelParam;
import org.example.params.ShortLinkUpdateParam;

public interface LinkSeniorService {

    int addLink(ShortLinkDO shortLinkDO);

    int del(ShortLinkDelParam shortLinkDelParam);

    int update(ShortLinkUpdateParam shortLinkUpdateParam);

    Boolean handlerAddShortLink(EventMessage eventMessage);

}
