package com.github.gv2011.m2timp;

import static com.github.gv2011.util.Verify.verify;

import com.github.gv2011.util.BeanUtils;
import com.github.gv2011.util.StringUtils;
import com.github.gv2011.util.beans.BeanBuilder;
import com.github.gv2011.util.icol.IList;
import com.github.gv2011.util.m2t.ArtifactRef;
import com.github.gv2011.util.m2t.M2t;
import com.github.gv2011.util.m2t.M2tFactory;
import com.github.gv2011.util.m2t.Type;

public final class M2tFactoryImp implements M2tFactory{

  @Override
  public M2t create() {
    return new M2tImp();
  }

  @Override
  public ArtifactRef parse(final String encoded) {
    return parseArtifactRef(encoded);
  }

  static ArtifactRef parseArtifactRef(final String encoded) {
    final IList<String> parts = StringUtils.split(encoded, ":");
    verify(parts, p->p.size()>=3 && p.size()<=5);
    final BeanBuilder<ArtifactRef> b = BeanUtils
      .beanBuilder(ArtifactRef.class)
      .set(ArtifactRef::groupId,    parts.get(0))
      .set(ArtifactRef::artifactId, parts.get(1))
      .set(ArtifactRef::version,    parts.get(2))
    ;
    if(parts.size()>3){
      b.set(ArtifactRef::type).to(Type.valueOf(parts.get(3)));
      if(parts.size()>4){
        b.set(ArtifactRef::classifier, parts.get(4));
      }
    }
    return b.build();
  }

}
