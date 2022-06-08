package org.example.generic;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MzCrudServiceTest {

  @Test
  void test() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      MzCrudService crud = beanScope.get(MzCrudService.class);
      ReadService<MzObj,Integer> read = beanScope.get(MzCrudService$DI.TYPE_ReadServiceMzObjInteger, null);
      CreateService<MzObj,Integer> create = beanScope.get(MzCrudService$DI.TYPE_CreateServiceMzObjInteger, null);

      assertNotNull(crud);
      assertThat(crud).isSameAs(read);
      assertThat(crud).isSameAs(create);

      assertThat(crud.iamCrud()).isEqualTo("MzCrud");
      assertThat(create.create(new MzObj())).isEqualTo(92);
      assertThat(read.get(42)).isNotEmpty();
    }
  }
}
