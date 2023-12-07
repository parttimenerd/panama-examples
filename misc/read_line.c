#include "stdio.h"
#include "stdlib.h"

int main(int argc, char *argv[]) {
  FILE* file = fopen(argv[1], "r");
  char* line = malloc(1024);
  fgets(line, 1024, file);
  printf("%s", line);
  fclose(file);
  free(line);
}