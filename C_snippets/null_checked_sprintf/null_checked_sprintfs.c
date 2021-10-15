#include <stdio.h>
#include <stdarg.h>
#include <string.h>

int benyUtl_sprintf(char *str, const char *format, ...);
int benyUtl_snprintf(char *str, size_t size, const char *format, ...);
int benyUtl_sprintf_check_for_null(const char *format, ...);

int benyUtl_sprintf(char *str, const char *format, ...) {
	/*	va_list, one for checking arguments.
	 *  another to pass to vsprintf.
	 */
	va_list args, args2;

	int i = 0, num_of_args = 0;
	char c = 1;

	va_start(args, format);	//intialize va_list
	va_copy(args2, args); 	//copy clean original va_list

	//check # of arguments
	while (c != '\0') {

		c = format[i];
		if (c == '%') {
			num_of_args++;
		}
		i++;
	}

	i = 0;

	char *value = NULL; //for reading values

	while (i < num_of_args) {

		value = va_arg(args , char *);	// read from va_list;
		if (!value)						// return negative when input == NIL
			return -1;
		i++;
	}
	va_end(args);						//a role for va_list args done.
	va_start(args2, format);

	return vsprintf(str, format, args2);	//run sprintf on arguments..

}

int benyUtl_snprintf(char *str, size_t size, const char *format, ...) {
	/*	va_list, one for checking arguments.
	 *  another to pass to vsprintf.
	 */
	va_list args, args2;

	int i = 0, num_of_args = 0;
	char c = 1;

	va_start(args, format);	//intialize va_list
	va_copy(args2, args); 	//copy clean original va_list

	//check # of arguments
	while (c != '\0') {

		c = format[i];
		if (c == '%') {
			num_of_args++;
		}
		i++;
	}

	i = 0;

	char *value = NULL; //for reading values

	while (i < num_of_args) {

		value = va_arg(args , char *);	// read from va_list;
		if (!value)						// return negative when input == NIL
			return -1;
		i++;
	}
	va_end(args);						//a role for va_list args done.
	va_start(args2, format);

	return vsnprintf(str, size, format, args2);
}

//test program, main
int main(void) {

	char buffer[100];

	//case with NULL
	if (benyUtl_snprintf(buffer, 100, "%d %c %s %u", 1, 'A', "STRING1", NULL)
			>= 0) {
		printf("First Read : \n%s\n", buffer);
	} else
		perror("null detected.\n");

	//case withOUT NULL
	if (benyUtl_snprintf(buffer, 100, "%d %c %s %u", 2, 'B', "STRINGB", 10)
			>= 0) {
		printf("Second Read : \n%s\n", buffer);
	} else
		perror("null detected.\n");

}
