FROM postgres:10
ADD scripts/ /docker-entrypoint-initdb.d/
ADD postgresql.conf /etc/postgresql/postgresql.conf
CMD ["-c", "config_file=/etc/postgresql/postgresql.conf"]