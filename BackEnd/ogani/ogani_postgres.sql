-- PostgreSQL database dump
-- Dumped from database version 15.0
-- Dumped by pg_dump version 15.0

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

-- Xóa enum type ERole nếu tồn tại
DROP TYPE IF EXISTS public.erole CASCADE;

--
-- Name: blog; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.blog (
    id bigint NOT NULL,
    content text,
    create_at timestamp without time zone,
    description text,
    title character varying(255),
    image_id bigint,
    user_id bigint
);

ALTER TABLE public.blog OWNER TO postgres;

--
-- Name: blog_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.blog_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.blog_id_seq OWNER TO postgres;

--
-- Name: blog_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.blog_id_seq OWNED BY public.blog.id;

--
-- Name: blog_tag; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.blog_tag (
    blog_id bigint NOT NULL,
    tag_id bigint NOT NULL
);

ALTER TABLE public.blog_tag OWNER TO postgres;

--
-- Name: category; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.category (
    id bigint NOT NULL,
    enable boolean NOT NULL,
    name character varying(255)
);

ALTER TABLE public.category OWNER TO postgres;

--
-- Name: category_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.category_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.category_id_seq OWNER TO postgres;

--
-- Name: category_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.category_id_seq OWNED BY public.category.id;

--
-- Name: image; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.image (
    id bigint NOT NULL,
    data bytea,
    name character varying(255),
    size bigint NOT NULL,
    type character varying(255),
    uploaded_by bigint
);

ALTER TABLE public.image OWNER TO postgres;

--
-- Name: image_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.image_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.image_id_seq OWNER TO postgres;

--
-- Name: image_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.image_id_seq OWNED BY public.image.id;

--
-- Name: product_image; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.product_image (
    image_id bigint NOT NULL,
    product_id bigint NOT NULL
);

ALTER TABLE public.product_image OWNER TO postgres;

--
-- Name: role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.role (
    id bigint NOT NULL,
    name character varying(20)
);

ALTER TABLE public.role OWNER TO postgres;

--
-- Name: role_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.role_id_seq OWNER TO postgres;

--
-- Name: role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.role_id_seq OWNED BY public.role.id;

--
-- Name: tag; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tag (
    id bigint NOT NULL,
    enable boolean NOT NULL,
    name character varying(255)
);

ALTER TABLE public.tag OWNER TO postgres;

--
-- Name: tag_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tag_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.tag_id_seq OWNER TO postgres;

--
-- Name: tag_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tag_id_seq OWNED BY public.tag.id;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    address character varying(255),
    country character varying(255),
    email character varying(255),
    enabled boolean NOT NULL,
    firstname character varying(255),
    lastname character varying(255),
    password character varying(255),
    phone character varying(255),
    state character varying(255),
    username character varying(255),
    verification_code character varying(64)
);

ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.users_id_seq OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;

--
-- Name: user_roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_roles (
    user_id bigint NOT NULL,
    role_id bigint NOT NULL
);

ALTER TABLE public.user_roles OWNER TO postgres;

--
-- Name: blog id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.blog ALTER COLUMN id SET DEFAULT nextval('public.blog_id_seq'::regclass);

--
-- Name: category id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category ALTER COLUMN id SET DEFAULT nextval('public.category_id_seq'::regclass);

--
-- Name: image id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.image ALTER COLUMN id SET DEFAULT nextval('public.image_id_seq'::regclass);

--
-- Name: role id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role ALTER COLUMN id SET DEFAULT nextval('public.role_id_seq'::regclass);

--
-- Name: tag id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tag ALTER COLUMN id SET DEFAULT nextval('public.tag_id_seq'::regclass);

--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);

--
-- Data for Name: image; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.image VALUES (14, NULL, 'blog-1.jpg', 0, 'image/jpeg', 1);
INSERT INTO public.image VALUES (15, NULL, 'blog-2.jpg', 0, 'image/jpeg', 1);
INSERT INTO public.image VALUES (16, NULL, 'blog-3.jpg', 0, 'image/jpeg', 1);
INSERT INTO public.image VALUES (17, NULL, 'blog-4.jpg', 0, 'image/jpeg', 1);
INSERT INTO public.image VALUES (18, NULL, 'blog-5.jpg', 0, 'image/jpeg', 1);

--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.users VALUES (1, NULL, NULL, 'admin@gmail.com', false, NULL, NULL, '$2a$10$eaywfhIHxT8M0jWZKhVKKucW6foBKFAK/byXFFcoK.s53SZhRrJOi', NULL, NULL, 'admin', NULL);

--
-- Data for Name: role; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.role VALUES (1, 'ROLE_ADMIN');
INSERT INTO public.role VALUES (2, 'ROLE_MODERATOR');
INSERT INTO public.role VALUES (3, 'ROLE_USER');

--
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.user_roles VALUES (1, 1);
INSERT INTO public.user_roles VALUES (1, 2);
INSERT INTO public.user_roles VALUES (1, 3);

--
-- Data for Name: blog; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.blog VALUES (1, 'Sed porttitor lectus nibh. Vestibulum ac diam sit amet quam vehicula elementum sed sit amet dui. Curabitur non nulla sit amet nisl tempus convallis quis ac lectus. Mauris blandit aliquet elit, eget tincidunt nibh pulvinar a. Vivamus magna justo, lacinia eget consectetur sed, convallis at tellus. Sed porttitor lectus nibh. Donec sollicitudin molestie malesuada. Curabitur non nulla sit amet nisl tempus convallis quis ac lectus. Proin eget tortor risus. Donec rutrum congue leo eget malesuada. Curabitur non nulla sit amet nisl tempus convallis quis ac lectus. Donec sollicitudin molestie malesuada. Nulla quis lorem ut libero malesuada feugiat. Curabitur arcu erat, accumsan id imperdiet et, porttitor at sem.\n\n', '2023-02-06 16:38:59', 'Sed quia non numquam modi tempora indunt ut labore et dolore magnam aliquam quaerat\n\n', '6 ways to prepare breakfast for 30', 14, 1);
INSERT INTO public.blog VALUES (2, 'Sed porttitor lectus nibh. Vestibulum ac diam sit amet quam vehicula elementum sed sit amet dui. Curabitur non nulla sit amet nisl tempus convallis quis ac lectus. Mauris blandit aliquet elit, eget tincidunt nibh pulvinar a. Vivamus magna justo, lacinia eget consectetur sed, convallis at tellus. Sed porttitor lectus nibh. Donec sollicitudin molestie malesuada. Curabitur non nulla sit amet nisl tempus convallis quis ac lectus. Proin eget tortor risus. Donec rutrum congue leo eget malesuada. Curabitur non nulla sit amet nisl tempus convallis quis ac lectus. Donec sollicitudin molestie malesuada. Nulla quis lorem ut libero malesuada feugiat. Curabitur arcu erat, accumsan id imperdiet et, porttitor at sem.\n\n', '2023-02-06 16:39:43', 'Sed quia non numquam modi tempora indunt ut labore et dolore magnam aliquam quaerat\n\n', 'Visit the clean farm in the US', 15, 1);
INSERT INTO public.blog VALUES (3, 'Sed porttitor lectus nibh. Vestibulum ac diam sit amet quam vehicula elementum sed sit amet dui. Curabitur non nulla sit amet nisl tempus convallis quis ac lectus. Mauris blandit aliquet elit, eget tincidunt nibh pulvinar a. Vivamus magna justo, lacinia eget consectetur sed, convallis at tellus. Sed porttitor lectus nibh. Donec sollicitudin molestie malesuada. Curabitur non nulla sit amet nisl tempus convallis quis ac lectus. Proin eget tortor risus. Donec rutrum congue leo eget malesuada. Curabitur non nulla sit amet nisl tempus convallis quis ac lectus. Donec sollicitudin molestie malesuada. Nulla quis lorem ut libero malesuada feugiat. Curabitur arcu erat, accumsan id imperdiet et, porttitor at sem.\n\n', '2023-02-06 16:40:10', 'Sed quia non numquam modi tempora indunt ut labore et dolore magnam aliquam quaerat\n\n', 'Cooking tips make cooking simple', 16, 1);
INSERT INTO public.blog VALUES (4, 'Sed porttitor lectus nibh. Vestibulum ac diam sit amet quam vehicula elementum sed sit amet dui. Curabitur non nulla sit amet nisl tempus convallis quis ac lectus. Mauris blandit aliquet elit, eget tincidunt nibh pulvinar a. Vivamus magna justo, lacinia eget consectetur sed, convallis at tellus. Sed porttitor lectus nibh. Donec sollicitudin molestie malesuada. Curabitur non nulla sit amet nisl tempus convallis quis ac lectus. Proin eget tortor risus. Donec rutrum congue leo eget malesuada. Curabitur non nulla sit amet nisl tempus convallis quis ac lectus. Donec sollicitudin molestie malesuada. Nulla quis lorem ut libero malesuada feugiat. Curabitur arcu erat, accumsan id imperdiet et, porttitor at sem.\n\n', '2023-02-06 16:40:38', 'Sed quia non numquam modi tempora indunt ut labore et dolore magnam aliquam quaerat\n\n', 'Cooking tips make cooking simple', 17, 1);
INSERT INTO public.blog VALUES (5, 'Sed porttitor lectus nibh. Vestibulum ac diam sit amet quam vehicula elementum sed sit amet dui. Curabitur non nulla sit amet nisl tempus convallis quis ac lectus. Mauris blandit aliquet elit, eget tincidunt nibh pulvinar a. Vivamus magna justo, lacinia eget consectetur sed, convallis at tellus. Sed porttitor lectus nibh. Donec sollicitudin molestie malesuada. Curabitur non nulla sit amet nisl tempus convallis quis ac lectus. Proin eget tortor risus. Donec rutrum congue leo eget malesuada. Curabitur non nulla sit amet nisl tempus convallis quis ac lectus. Donec sollicitudin molestie malesuada. Nulla quis lorem ut libero malesuada feugiat. Curabitur arcu erat, accumsan id imperdiet et, porttitor at sem.\n\n', '2023-02-06 16:42:36', 'Sed quia non numquam modi tempora indunt ut labore et dolore magnam aliquam quaerat\n\n', 'The Moment You Need To Remove Garlic From The Menu', 17, 1);
INSERT INTO public.blog VALUES (6, 'Sed porttitor lectus nibh. Vestibulum ac diam sit amet quam vehicula elementum sed sit amet dui. Curabitur non nulla sit amet nisl tempus convallis quis ac lectus. Mauris blandit aliquet elit, eget tincidunt nibh pulvinar a. Vivamus magna justo, lacinia eget consectetur sed, convallis at tellus. Sed porttitor lectus nibh. Donec sollicitudin molestie malesuada. Curabitur non nulla sit amet nisl tempus convallis quis ac lectus. Proin eget tortor risus. Donec rutrum congue leo eget malesuada. Curabitur non nulla sit amet nisl tempus convallis quis ac lectus. Donec sollicitudin molestie malesuada. Nulla quis lorem ut libero malesuada feugiat. Curabitur arcu erat, accumsan id imperdiet et, porttitor at sem.\n\n', '2023-02-06 16:43:00', 'Sed quia non numquam modi tempora indunt ut labore et dolore magnam aliquam quaerat\n\n', 'Cooking tips make cooking simple', 18, 1);

--
-- Data for Name: blog_tag; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.blog_tag VALUES (2, 1);
INSERT INTO public.blog_tag VALUES (4, 1);
INSERT INTO public.blog_tag VALUES (6, 1);
INSERT INTO public.blog_tag VALUES (2, 2);
INSERT INTO public.blog_tag VALUES (4, 2);
INSERT INTO public.blog_tag VALUES (5, 2);
INSERT INTO public.blog_tag VALUES (1, 3);
INSERT INTO public.blog_tag VALUES (3, 3);
INSERT INTO public.blog_tag VALUES (5, 3);
INSERT INTO public.blog_tag VALUES (1, 4);
INSERT INTO public.blog_tag VALUES (3, 4);
INSERT INTO public.blog_tag VALUES (6, 4);

--
-- Data for Name: category; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.category VALUES (1, true, 'Fresh Meat');
INSERT INTO public.category VALUES (2, true, 'Vegetables');
INSERT INTO public.category VALUES (3, true, 'Fruit & Nut Gifts');
INSERT INTO public.category VALUES (4, true, 'Fresh Berries');
INSERT INTO public.category VALUES (5, true, 'Ocean Foods');
INSERT INTO public.category VALUES (6, true, 'Butter & Eggs');
INSERT INTO public.category VALUES (7, true, 'Fastfood');
INSERT INTO public.category VALUES (8, true, 'Fresh Onion');
INSERT INTO public.category VALUES (9, true, 'Papayaya & Crisps');
INSERT INTO public.category VALUES (10, true, 'Oatmeal');
INSERT INTO public.category VALUES (11, true, 'Fresh Bananas');

--
-- Data for Name: tag; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.tag VALUES (1, true, 'Beauty');
INSERT INTO public.tag VALUES (2, true, 'Food');
INSERT INTO public.tag VALUES (3, true, 'LifeStyle');
INSERT INTO public.tag VALUES (4, true, 'Travel');

--
-- Name: blog blog_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.blog
    ADD CONSTRAINT blog_pkey PRIMARY KEY (id);

--
-- Name: blog_tag blog_tag_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.blog_tag
    ADD CONSTRAINT blog_tag_pkey PRIMARY KEY (blog_id, tag_id);

--
-- Name: category category_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT category_pkey PRIMARY KEY (id);

--
-- Name: image image_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.image
    ADD CONSTRAINT image_pkey PRIMARY KEY (id);

--
-- Name: product_image product_image_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_image
    ADD CONSTRAINT product_image_pkey PRIMARY KEY (image_id, product_id);

--
-- Name: role role_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);

--
-- Name: tag tag_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tag
    ADD CONSTRAINT tag_pkey PRIMARY KEY (id);

--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);

--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);

--
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id);

--
-- Name: blog FKeh12tt593aj2kqpwf2m3vet9j; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.blog
    ADD CONSTRAINT FKeh12tt593aj2kqpwf2m3vet9j FOREIGN KEY (image_id) REFERENCES public.image(id);

--
-- Name: blog FKpxk2jtysqn41oop7lvxcp6dqq; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.blog
    ADD CONSTRAINT FKpxk2jtysqn41oop7lvxcp6dqq FOREIGN KEY (user_id) REFERENCES public.users(id);

--
-- Name: blog_tag FKd0y9mfvb4wsvn1yi3a9jhsase; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.blog_tag
    ADD CONSTRAINT FKd0y9mfvb4wsvn1yi3a9jhsase FOREIGN KEY (blog_id) REFERENCES public.blog(id);

--
-- Name: blog_tag FKt7qwebglmm62nfymnl5xwpbws; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.blog_tag
    ADD CONSTRAINT FKt7qwebglmm62nfymnl5xwpbws FOREIGN KEY (tag_id) REFERENCES public.tag(id);

--
-- Name: image FK6q9lwl1j82nmm8n75cxcuxc1p; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.image
    ADD CONSTRAINT FK6q9lwl1j82nmm8n75cxcuxc1p FOREIGN KEY (uploaded_by) REFERENCES public.users(id);

--
-- Name: user_roles FK55itppkw3i07do3h7qoclqd4k; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT FK55itppkw3i07do3h7qoclqd4k FOREIGN KEY (user_id) REFERENCES public.users(id);

--
-- Name: user_roles FKrhfovtciq1l558cw6udg0h0d3; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT FKrhfovtciq1l558cw6udg0h0d3 FOREIGN KEY (role_id) REFERENCES public.role(id);

-- Cập nhật các sequence để bắt đầu từ giá trị lớn hơn giá trị hiện có
-- Thêm vào cuối file SQL
SELECT setval('public.users_id_seq', (SELECT MAX(id) FROM public.users), true);
SELECT setval('public.role_id_seq', (SELECT MAX(id) FROM public.role), true);
SELECT setval('public.blog_id_seq', (SELECT MAX(id) FROM public.blog), true);
SELECT setval('public.tag_id_seq', (SELECT MAX(id) FROM public.tag), true);
SELECT setval('public.category_id_seq', (SELECT MAX(id) FROM public.category), true);
SELECT setval('public.image_id_seq', (SELECT MAX(id) FROM public.image), true);

-- PostgreSQL database dump complete
-- 