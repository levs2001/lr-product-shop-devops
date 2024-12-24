FROM nginx:1.27.3
CMD echo "Hi, I'm inside"
ENTRYPOINT ["echo", "some entrypoint"]