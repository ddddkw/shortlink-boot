package org.example.service;

import org.example.entity.ShortLinkDO;
import org.example.model.EventMessage;
import org.example.params.ShortLinkAddParam;
import org.example.params.ShortLinkDelParam;
import org.example.params.ShortLinkUpdateParam;
import org.example.utils.JsonData;

public interface LinkSeniorService {

    JsonData addLink(ShortLinkAddParam shortLinkAddParam);

    int del(ShortLinkDelParam shortLinkDelParam);

    int update(ShortLinkUpdateParam shortLinkUpdateParam);

    Boolean handlerAddShortLink(EventMessage eventMessage);

    Boolean handlerUpdateShortLink(EventMessage eventMessage);

    Boolean handlerDelShortLink(EventMessage eventMessage);

}
