package com.climbers.hub.social.service;

import com.climbers.hub.social.domain.SocialProvider;

/** Provider-specific parsers map their verified response to this common identity. */
public record SocialIdentity(SocialProvider provider, String subject, String nickname, String verifiedEmail) {}
